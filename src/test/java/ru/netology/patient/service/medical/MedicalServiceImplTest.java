package ru.netology.patient.service.medical;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.alert.SendAlertServiceImpl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MedicalServiceImplTest {
    private ByteArrayOutputStream output = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    public void cleanUpStreams() {
        System.setOut(null);
    }

    @Test
    @DisplayName("Отправляем сообщение, если показатели крови отличаются")
    void shouldSendAlertWhenNotEqualBlood() {
        PatientInfo patientInfo = new PatientInfo("Ivan","Ivanov",
                LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));
        BloodPressure currentPressure = new BloodPressure(60, 120);

        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById("userId_1")).thenReturn(patientInfo);

        SendAlertService sendAlertService = new SendAlertServiceImpl();
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkBloodPressure("userId_1", currentPressure);

        String message = String.format("Warning, patient with id: %s, need help\r\n", patientInfo.getId());
        assertEquals(message, output.toString());
    }

    @Test
    @DisplayName("Показатели крови в норме, сообщение не отправляем")
    void shouldNotSendAlertWhenEqualBlood() {
        PatientInfo patientInfo = new PatientInfo("Ivan","Ivanov",
                LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));
        BloodPressure currentPressure = new BloodPressure(120, 80);

        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById("userId_1")).thenReturn(patientInfo);

        SendAlertService sendAlertService = new SendAlertServiceImpl();
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkBloodPressure("userId_1", currentPressure);

        assertEquals("", output.toString());
    }

    @Test
    @DisplayName("Отправляем сообщение, если показатели температуры отличаются")
    void shouldSendAlertWhenNotEqualTemperature() {
        PatientInfo patientInfo = new PatientInfo("Ivan","Ivanov",
                LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));

        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById("userId_1")).thenReturn(patientInfo);

        SendAlertService sendAlertService = new SendAlertServiceImpl();
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkTemperature("userId_1", new BigDecimal("34.0"));

        String message = String.format("Warning, patient with id: %s, need help\r\n", patientInfo.getId());
        assertEquals(message, output.toString());
    }

    @Test
    @DisplayName("Показатели температуры в норме, сообщение не отправляем")
    void shouldNotSendAlertWhenEqualTemperature() {
        PatientInfo patientInfo = new PatientInfo("Ivan","Ivanov",
                LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));

        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById("userId_1")).thenReturn(patientInfo);

        SendAlertService sendAlertService = new SendAlertServiceImpl();
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
        medicalService.checkTemperature("userId_1", new BigDecimal("37.5"));

        assertEquals("", output.toString());
    }

    @Test
    @DisplayName("Пациент не найден")
    void shouldExceptionWhenPatientNotFound() {
        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById("userId_1")).thenReturn(null);

        SendAlertService sendAlertService = new SendAlertServiceImpl();
        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);

        assertThrows(RuntimeException.class,
                () -> medicalService.checkTemperature("userId_1", new BigDecimal("34.0")));
    }

    @Test
    @DisplayName("Вызов метода send у класса SendAlertServiceImpl")
    void checkCallMethodSend() {
        PatientInfo patientInfo = new PatientInfo("Ivan","Ivanov",
                LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)));

        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        Mockito.when(patientInfoFileRepository.getById("userId_1")).thenReturn(patientInfo);

        SendAlertServiceImpl alertService = Mockito.mock(SendAlertServiceImpl.class);

        MedicalService medicalService = new MedicalServiceImpl(patientInfoFileRepository, alertService);
        medicalService.checkTemperature("userId_1", new BigDecimal("34.0"));

        Mockito.verify(alertService, Mockito.times(1)).send(Mockito.any());
    }
}

