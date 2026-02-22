package microservice.appointmentservice.services;

import org.springframework.stereotype.Service;
import microservice.appointmentservice.models.Appointment;
import microservice.appointmentservice.dto.AppointmentDTO;
import microservice.appointmentservice.repositories.IAppointmentRepository;
import java.util.List;

@Service
public class AppointmentService {
    private final IAppointmentRepository appointmentRepository;

    public AppointmentService(IAppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment createAppointment(AppointmentDTO appointmentDTO) {
        Appointment appointment = Appointment.builder()
                .date(appointmentDTO.getDate())
                .time(appointmentDTO.getTime())
                .patientId(appointmentDTO.getPatientId())
                .build();
        return appointmentRepository.save(appointment);
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    public List<Appointment> getAppointmentByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public Appointment updateAppointment(Long id, AppointmentDTO appointmentDTO) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (appointmentDTO.getDate() != null)
            appointment.setDate(appointmentDTO.getDate());
        if (appointmentDTO.getTime() != null)
            appointment.setTime(appointmentDTO.getTime());
        if (appointmentDTO.getPatientId() != null)
            appointment.setPatientId(appointmentDTO.getPatientId());
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}