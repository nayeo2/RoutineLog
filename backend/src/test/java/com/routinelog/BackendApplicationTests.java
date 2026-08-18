package com.routinelog;

import com.routinelog.dailyroutine.repository.DailyRoutineRepository;
import com.routinelog.routine.repository.RoutineRepository;
import com.routinelog.user.repository.UserRepository;
import com.routinelog.video.repository.VideoRepository;
import com.routinelog.vlog.repository.DailyVlogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private RoutineRepository routineRepository;

	@MockitoBean
	private DailyRoutineRepository dailyRoutineRepository;

	@MockitoBean
	private VideoRepository videoRepository;

	@MockitoBean
	private DailyVlogRepository dailyVlogRepository;

	@Test
	void contextLoads() {
	}

}
