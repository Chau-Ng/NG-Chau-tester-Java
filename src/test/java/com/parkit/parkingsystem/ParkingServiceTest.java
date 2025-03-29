package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	private void setUpForExitingTest() {
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			Ticket ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");
			when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	private void setupCommonMocks() {
		try {
			when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
			when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	public void processExitingVehicleTest() {
		setUpForExitingTest();
		setupCommonMocks();
		when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

		parkingService.processExitingVehicle();

		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, times(1)).getNbTicket(anyString());

		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		ParkingSpot parkingSpot = ticket.getParkingSpot();
		assertTrue(parkingSpot.isAvailable(), "The ParkingSpot should be marked as available after the vehicle exits");
	}

	@Test
	public void ProcessIncomingVehicle() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
			when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(parkingSpot.getId());
			when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}

		parkingService.processIncomingVehicle();

		verify(parkingSpotDAO, times(1)).updateParking(parkingSpot);
		verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));

	}

	@Test
	public void processExitingVehicleTestUnableUpdate() throws Exception {
		when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
		setUpForExitingTest();

		parkingService.processExitingVehicle();

		verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));

	}

	@Test
	public void testGetNextParkingNumberIfAvailable() {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

		assertTrue(result != null && result.getId() == 1 && result.isAvailable(),
				"The parking spot returned should have ID 1 and be available");
	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

		assertTrue(result == null, "The method should return null when no parking spot is available.");
	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

		when(inputReaderUtil.readSelection()).thenReturn(3);

		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

		assertTrue(result == null, "The method should return null when an incorrect selection is provided.");
	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingFull() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

		assertTrue(result == null, "The method should return null when the parking is full.");
	}

}
