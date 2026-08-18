package com.routinelog;

import com.routinelog.user.repository.UserRepository;
import com.routinelog.routine.repository.RoutineRepository;
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

	@Test
	void contextLoads() {
	}

}
