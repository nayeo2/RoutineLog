package com.routinelog;

import com.routinelog.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

}
