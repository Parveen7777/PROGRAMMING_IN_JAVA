# Task 05: static members and object life cycle

ParkingTicket.counter is static so every ticket gets a unique number (see Task11/src/parking/model/ParkingTicket.java). Tickets become eligible for GC after active[activeCount] = null in ParkingService.exit. Run Task04/TicketDemo for the pass-by-value proof.
