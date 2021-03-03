package com.bookmyshow.bookmyshowapi.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bookmyshow.bookmyshowapi.models.PreBooking;
import com.bookmyshow.bookmyshowapi.models.Show;
import com.bookmyshow.bookmyshowapi.repositories.PreBookingRepository;
import com.bookmyshow.bookmyshowapi.repositories.ShowRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class SchedulingService {

	@Autowired
	private PreBookingRepository preBookingRepository;

	@Autowired
	private ShowRepository showRepository;
	
	private Long tenMinutesInMilliSeconds = (long)(10*60*1000);
	private Long thirtyMinutesInMilliSeconds = (long)(30*60*1000);
	
	@Scheduled(fixedRate = 5*60*1000)
	public void checkPreBookingTimeouts() {
		List<PreBooking> preBookingList = preBookingRepository.findAll();
		List<PreBooking> timedOutBookings = new ArrayList<>();
		
		for(PreBooking preBooking : preBookingList) {
			Date preBookingDate = preBooking.getCreatedAt();
			Date currentDate = new Date();
			long diffInBookingTime = currentDate.getTime() - preBookingDate.getTime();
			if(diffInBookingTime > tenMinutesInMilliSeconds) {
				log.info("deleting the bookings of the user: " + preBooking.getUser().getName() + " due to timeout");
				timedOutBookings.add(preBooking);
			}
		}
		preBookingRepository.deleteAll(timedOutBookings);
	}
	
	@Scheduled(fixedRate = 5*60*1000)
	public void LockingShowBookings() {
		List<Show> showList = showRepository.findAllByIsAvailableForBooking(true);
		for(Show show : showList) {
			Long showTime = show.getShowTime().getTime();
			Long currentTime = new Date().getTime();
			Long diffInShowTimings = currentTime - showTime;
			if(diffInShowTimings >= thirtyMinutesInMilliSeconds) {
				show.setAvailableForBooking(false);
				log.info("Online Booking has been stopped since show " + show.getId() + " is starting in next thirty minutes");
			}
		}
	}
}
