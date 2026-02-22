package microservice.appointmentservice.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import java.util.List;
import microservice.appointmentservice.services.AppointmentService;
import microservice.appointmentservice.services.PatientLookupService;
import microservice.appointmentservice.models.Appointment;
import microservice.appointmentservice.repositories.IAppointmentRepository;
import microservice.appointmentservice.dto.AppointmentDTO;
import microservice.appointmentservice.dto.PatientDTO;
import microservice.appointmentservice.exceptions.PatientServiceUnavailableException;
import feign.FeignException;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentServiceController {
    private final AppointmentService appointmentService;
    private final PatientLookupService patientLookupService;
    private final IAppointmentRepository appointmentRepository;

    public AppointmentServiceController(AppointmentService appointmentService,
            PatientLookupService patientLookupService, IAppointmentRepository appointmentRepository) {
        this.appointmentService = appointmentService;
        this.patientLookupService = patientLookupService;
        this.appointmentRepository = appointmentRepository;
    }

    @PostMapping()
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody AppointmentDTO appointmentDTO) {
        try {
            if (appointmentDTO.getPatientId() != null) {
                var patientExists = this.isPatientExists(appointmentDTO.getPatientId());
                if (!patientExists)
                    return ResponseEntity.badRequest().body(null);
            }

            Appointment appointment = appointmentService.createAppointment(appointmentDTO);
            return ResponseEntity.ok(
                    AppointmentDTO.builder()
                            .id(appointment.getId())
                            .date(appointment.getDate())
                            .time(appointment.getTime())
                            .patientId(appointment.getPatientId())
                            .build());
        } catch (PatientServiceUnavailableException e) {
            return ResponseEntity.status(503).build();
        }
    }

    @GetMapping()
    public ResponseEntity<List<AppointmentDTO>> getAppointments() {
        var appointments = appointmentService.getAllAppointments().stream().map(appointment -> AppointmentDTO.builder()
                .id(appointment.getId())
                .date(appointment.getDate())
                .time(appointment.getTime())
                .patientId(appointment.getPatientId())
                .build()).toList();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        try {
            var patientExists = this.isPatientExists(patientId);
            if (!patientExists)
                return ResponseEntity.badRequest().body(null);

            PatientDTO patient = patientLookupService.getPatientById(patientId);

            var appointments = appointmentService.getAppointmentByPatientId(patientId).stream()
                    .map(appointment -> AppointmentDTO.builder()
                            .id(appointment.getId())
                            .date(appointment.getDate())
                            .time(appointment.getTime())
                            .patient(patient)
                            .build())
                    .toList();
            return ResponseEntity.ok(appointments);
        } catch (PatientServiceUnavailableException e) {
            return ResponseEntity.status(503).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDTO> updateAppointment(@PathVariable Long id,
            @RequestBody AppointmentDTO requestBody) {
        try {
            var appointment = appointmentRepository.getReferenceById(id);

            if (requestBody.getDate() != null)
                appointment.setDate(requestBody.getDate());
            if (requestBody.getTime() != null)
                appointment.setTime(requestBody.getTime());
            if (requestBody.getPatientId() != null)
                appointment.setPatientId(requestBody.getPatientId());

            appointmentRepository.save(appointment);

            return ResponseEntity.ok(AppointmentDTO.builder()
                    .id(appointment.getId())
                    .date(appointment.getDate())
                    .time(appointment.getTime())
                    .patientId(appointment.getPatientId())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

    }

    private boolean isPatientExists(Long patientId) {
        return patientLookupService.patientExists(patientId);
    }
}
