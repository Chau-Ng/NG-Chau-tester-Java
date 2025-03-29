package com.parkit.parkingsystem;

import com.parkit.parkingsystem.service.InteractiveShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
/**
 * javadoc
 * un commentaire 
 */
public class App {
    private static final Logger logger = LogManager.getLogger("App");

    public static void main(String[] args) {
        logger.info("Initializing Parking System");

        // Initialize dependencies
        InputReaderUtil inputReaderUtil = new InputReaderUtil();
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        TicketDAO ticketDAO = new TicketDAO();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Create InteractiveShell instance
        InteractiveShell interactiveShell = new InteractiveShell(inputReaderUtil, parkingService);

        // Call the loadInterface method
        interactiveShell.loadInterface();
    }
}