package com.routinelog.dailyroutine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.dailyroutine.service.DailyRoutineService;
import org.junit.jupiter.api.Test;

class DailyRoutineControllerTest {

	private final DailyRoutineController dailyRoutineController =
		new DailyRoutineController(mock(DailyRoutineService.class));

	@Test
	void findAllByDateRejectsMissingDate() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyRoutineController.findAllByDate(1L, null)
		);

		assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
	}

	@Test
	void findAllByDateRejectsInvalidDate() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyRoutineController.findAllByDate(1L, "18-08-2026")
		);

		assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
	}
}
