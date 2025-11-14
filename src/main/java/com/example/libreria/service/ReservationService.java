package com.example.libreria.service;

import com.example.libreria.dto.ReservationRequestDTO;
import com.example.libreria.dto.ReservationResponseDTO;
import com.example.libreria.dto.ReturnBookRequestDTO;
import com.example.libreria.dto.UserResponseDTO;
import com.example.libreria.model.Book;
import com.example.libreria.model.Reservation;
import com.example.libreria.model.User;
import com.example.libreria.repository.BookRepository;
import com.example.libreria.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    
    private static final BigDecimal LATE_FEE_PERCENTAGE = new BigDecimal("0.15"); // 15% por día
    
    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final UserService userService;
    
    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO requestDTO) {
        ReservationResponseDTO dto = new ReservationResponseDTO();

        // TODO: Implementar la creación de una reserva
        // Validar que el usuario existe
        UserResponseDTO user =userService.getUserById(requestDTO.getUserId());
        if (Objects.equals(user, new UserResponseDTO())) {
            // Validar que el libro existe y está disponible
            if(bookRepository.existsByExternalId(requestDTO.getBookExternalId())){
                // Crear la reserva
               Reservation r = new Reservation();
               List<Reservation> reservaciones = new ArrayList<>();
               User user1 = new User(user.getId(),user.getName(),user.getEmail(),user.getPhoneNumber(),
                       user.getCreatedAt(), reservaciones);
               r.setUser(user1);
               r.setBook(bookRepository.getReferenceById(requestDTO.getBookExternalId()));
               r.setRentalDays(requestDTO.getRentalDays());
               r.setStartDate(requestDTO.getStartDate());
               Reservation resultado=  reservationRepository.save(r);
               dto = convertToDTO(resultado);
                // Reducir la cantidad disponible
                bookService.decreaseAvailableQuantity(requestDTO.getBookExternalId());

            }    else{
                throw new RuntimeException("No existe un libro con ese id" );
            }
        }
        else{
            throw new RuntimeException("No existe un usuario con ese id" );
        }

        return dto;

    }
    
    @Transactional
    public ReservationResponseDTO returnBook(Long reservationId, ReturnBookRequestDTO returnRequest) {


        ReservationResponseDTO dto = new ReservationResponseDTO();
        // TODO: Implementar la devolución de un libro
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + reservationId));
        
        if (reservation.getStatus() != Reservation.ReservationStatus.ACTIVE) {
            throw new RuntimeException("La reserva ya fue devuelta");
        }
        
        LocalDate returnDate = returnRequest.getReturnDate();
        reservation.setActualReturnDate(returnDate);


        // Calcular tarifa por demora si hay retraso
        if (returnDate.isAfter(reservation.getExpectedReturnDate())) {
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(
                    reservation.getExpectedReturnDate(),
                    returnDate
            );

            BigDecimal lateFee = calculateLateFee(reservation.getBook().getPrice(), daysLate);
            reservation.setLateFee(lateFee);
            reservation.setStatus(Reservation.ReservationStatus.OVERDUE);

        } else {
            BigDecimal free= calculateTotalFee(reservation.getBook().getPrice(), reservation.getRentalDays());
            reservation.setTotalFee(free);
            reservation.setStatus(Reservation.ReservationStatus.RETURNED);
        }

        Reservation updatedReservation = reservationRepository.save(reservation);

        // Aumentar la cantidad disponible
        bookService.increaseAvailableQuantity(reservation.getBook().getExternalId());


        return convertToDTO(updatedReservation);


    }
    
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
        return convertToDTO(reservation);
    }
    
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getActiveReservations() {
        return reservationRepository.findByStatus(Reservation.ReservationStatus.ACTIVE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getOverdueReservations() {
        return reservationRepository.findOverdueReservations().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private BigDecimal calculateTotalFee(BigDecimal dailyRate, Integer rentalDays) {
        // TODO: Implementar el cálculo del total de la reserva
        if (dailyRate == null || rentalDays == null || rentalDays <= 0) {
            return BigDecimal.ZERO;
        }

        return dailyRate.multiply(new BigDecimal(rentalDays))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateLateFee(BigDecimal bookPrice, long daysLate) {
        // 15% del precio del libro por cada día de demora
        // TODO: Implementar el cálculo de la multa por demora
        if (bookPrice == null || daysLate <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal feePerDay = bookPrice.multiply(LATE_FEE_PERCENTAGE);
        return feePerDay.multiply(new BigDecimal(daysLate))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(reservation.getId());
        dto.setUserId(reservation.getUser().getId());
        dto.setUserName(reservation.getUser().getName());
        dto.setBookExternalId(reservation.getBook().getExternalId());
        dto.setBookTitle(reservation.getBook().getTitle());
        dto.setRentalDays(reservation.getRentalDays());
        dto.setStartDate(reservation.getStartDate());
        dto.setExpectedReturnDate(reservation.getExpectedReturnDate());
        dto.setActualReturnDate(reservation.getActualReturnDate());
        dto.setDailyRate(reservation.getDailyRate());
        dto.setTotalFee(reservation.getTotalFee());
        dto.setLateFee(reservation.getLateFee());
        dto.setStatus(reservation.getStatus());
        dto.setCreatedAt(reservation.getCreatedAt());
        return dto;
    }
}

