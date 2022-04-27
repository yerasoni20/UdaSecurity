package com.udacity.catpoint.SecurityService;

import com.udacity.catpoint.Image_Package.service.CatImageService;
import com.udacity.catpoint.Image_Package.service.FakeImageService;
import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.*;
import com.udacity.catpoint.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecuritySeviceTesting {
    private SecurityService service;

    private final String randomUUID = UUID.randomUUID ( ).toString ( );
    @Mock
    private SecurityRepository repository;

    @Mock
    private FakeImageService imageService;

    @Mock
    private StatusListener listener;

    private Sensor sensor;

    private Sensor createNewSensor ( ) {
        return new Sensor ( randomUUID, SensorType.DOOR );
    }

    @BeforeEach
    void init()
    {
        service=new SecurityService(repository,imageService);
        //sensor=new Sensor("MySensor", SensorType.DOOR);
        sensor = createNewSensor();
    }

    //TEST 1
    @Test
    public void getIfAlarmArmedNoAlarmStatusAndSensorActivated_SetToAlarmPendingStatus()
    {

        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        service.changeSensorActivationStatus(sensor, true);
        verify(repository,times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    //TEST 2
    @Test
    public void getIfAlarmArmedPendingAlarmStatusAndSensorActivated_SetToAlarmStatus()
    {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        service.changeSensorActivationStatus(sensor,true);
        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //TEST 3
    @Test
    public void getIfArmedAlarmPendingAlarmStatusAndNotActivatedSensor_SetToNoAlarmStatus()
    {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(false);
        service.changeSensorActivationStatus(sensor);
    }

    //TEST 4
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @MockitoSettings(strictness= Strictness.LENIENT)
    public void getIfArmedAlarmStatus_SensorChanged_NoAlarmStatusChanged(boolean alarmstatus)
    {
        when (repository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        service.changeSensorActivationStatus(sensor,alarmstatus);
        verify(repository,never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //TEST 5
    @Test
    public void getIfSensorActivatedWhileActiveSensorAndPendingAlarm_ChangeAlarmStatusToAlarm()
    {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        //sensor.setActive(true);
        service.changeSensorActivationStatus(sensor, true);
        verify(repository,times(1)).setAlarmStatus(AlarmStatus.ALARM);

        /*when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        service.changeSensorActivationStatus(sensor, true);

        verify(repository, times(1)).setAlarmStatus(AlarmStatus.ALARM);*/
    }

    //TEST 6
    @ParameterizedTest
    @EnumSource(value=AlarmStatus.class, names={"PENDING_ALARM","NO_ALARM"})
    @MockitoSettings(strictness=Strictness.LENIENT)
    public void getIfNoAlarmStatus_OneDeactivatedSensor_AlarmStatusNotChanged(AlarmStatus status)
    {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(repository.getAlarmStatus()).thenReturn(status);
        sensor.setActive(true);
        service.changeSensorActivationStatus(sensor, false);

        verify(repository, never()).setArmingStatus(ArmingStatus.DISARMED);
    }

    //TEST 7
    @Test
    public void getIfArmedHomeArmingStatusAndImageServiceIdentifiedCat_SetAlarmStatus()
    {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        BufferedImage catImage = new BufferedImage(300, 225, TYPE_INT_ARGB);
        when(imageService.imageContainsCat(catImage, 50.0f)).thenReturn(Boolean.TRUE);
        service.processImage(catImage);
        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //TEST 8
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void getIfCatNotIdentifiedAndDeactivatedSensor_SetNoAlarmStatus()
    {
        Set<Sensor> sensors=getAllSensors(3,false);
        when(repository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(),ArgumentMatchers.anyFloat())).thenReturn(Boolean.FALSE);
        service.processImage(mock(BufferedImage.class));
        verify(repository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //TEST 9
    @Test
    public void getIfDisarmedArmingStatus_SetToNoAlarmStatus()
    {
        service.setArmingStatus(ArmingStatus.DISARMED);
        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    public Set<Sensor> getAllSensors(int numberOfSensors, boolean activeSensor) {
        Set<Sensor> sensorHashSet = new HashSet<>();
        for (int i = 0; i < numberOfSensors; i++) {
            sensorHashSet.add(new Sensor(String.valueOf(i), SensorType.DOOR));
        }
        for(Sensor sensorSearch : sensorHashSet){
            sensorSearch.setActive(activeSensor);
        }
        return sensorHashSet;
    }

    //TEST 10
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME","ARMED_AWAY"})
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void getIfArmedArmingStatus_SetAllDeactivatedSensors(ArmingStatus status)
    {
        Set<Sensor> sensors = getAllSensors(3, true);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(repository.getSensors()).thenReturn(sensors);
        service.setArmingStatus(status);

        service.getSensors().forEach(sensor -> {
            assertTrue(sensor.getActive());
        });
    }

    //TEST 11
    @Test
    public void getIfDisarmedArmingStatusAndCatIdentifiesThenArmed_HomeArmingStatus_SetToAlarmStatus()
    {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        service.processImage(mock(BufferedImage.class));
        verify(repository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void addAndRemoveStatusListener()
    {
        service.addStatusListener(listener);
        service.removeStatusListener(listener);
    }

    @Test
    public void addAndRemoveSensor()
    {
        service.addSensor(sensor);
        service.removeSensor(sensor);
    }

    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM"})
    @MockitoSettings(strictness = Strictness.LENIENT)
    void getIfSensorActivatedAndSystemDisarmed_noChangesToArmingState(AlarmStatus status) {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(repository.getAlarmStatus()).thenReturn(status);
        service.changeSensorActivationStatus(sensor, true);
        verify(repository, never()).setArmingStatus(ArmingStatus.DISARMED);
    }

    @Test
    void getIfAlarmStateAndSystemDisarmed_changeStatusToPending()
    {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        service.changeSensorActivationStatus(sensor);
        verify(repository,times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }
}