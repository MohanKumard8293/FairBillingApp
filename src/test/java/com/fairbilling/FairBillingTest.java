package com.fairbilling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fairbilling.FairBilling;
import com.fairbilling.FairBillingException;
import com.fairbilling.LineInPieces;
import com.fairbilling.UserResult;
import com.fairbilling.UserSession;

public class FairBillingTest {

	private FairBilling fairBilling;

	@Before
	public void setup() {
		fairBilling = new FairBilling();
	}

	// This verifies that the file is loaded - Empty or Not
	@Test
	public void testProcessLoadFile() throws IOException {
		List<String> file = fairBilling.loadFileToList("./src/test/java/com/fairbilling/test.log");
		assertFalse(file.isEmpty());
		assertEquals("Test file loaded correctly", 11, file.size());
	}

	// This verifies that the Containing Lines - Null or Empty
	@Test
	public void testProcessLineNullOrEmpty() {
		assertFalse(fairBilling.parseLine(null).isValid());
		assertFalse(fairBilling.parseLine("").isValid());
	}

	// This verifies that the Containing Invalid Lines
	@Test
	public void testProcessLineInvalid() {
		assertFalse(fairBilling.parseLine("xxx").isValid());
	}

	// This verifies that the Containing Line is Valid or not (Partial Valid)
	@Test
	public void testProcessLineInvalidPartial() {
		assertFalse(fairBilling.parseLine("14:02:03 ALICE99").isValid());
	}

	// This verifies that the Containing Line is Valid or not
	@Test
	public void testProcessLineValidLine() {
		assertTrue(fairBilling.parseLine("14:02:03 ALICE99 Start").isValid());
	}

	// This verifies that the Containing Line is Starting with and Ending time is null
	@Test
	public void testProcessLineNoEnding() {
		List<UserSession> userList = null;
		userList = fairBilling.processLine(new LineInPieces("14", "02", "11", "ALICE99", "Start"), userList);
		assertNotNull(userList);
		assertEquals(1, userList.size());
		assertEquals("UserSession [userId=ALICE99, startTime=14:02:11, endTime=null]", userList.get(0).toString());
	}

	// This verifies that the Containing Line is Start with one, where start date is already set, But End Date as Null
	@Test
	public void testProcessLineOneRowStart() {

		List<UserSession> userList = new ArrayList<>();
		UserSession userSession = new UserSession("ALICE99", LocalTime.of(14, 01, 56), null);
		userList.add(userSession);

		userList = fairBilling.processLine(new LineInPieces("14", "02", "11", "ALICE99", "End"), userList);
		assertNotNull(userList);
		assertEquals(1, userList.size());
		assertEquals("UserSession [userId=ALICE99, startTime=14:01:56, endTime=14:02:11]", userList.get(0).toString());
	}

	// This verifies that the Containing Two End Rows
	@Test
	public void testProcessLineTwoEnds() {
		List<LineInPieces> pList = Arrays.asList(new LineInPieces("14", "02", "11", "ALICE99", "End"),
				new LineInPieces("14", "02", "12", "ALICE99", "End"));

		Map<String, List<UserSession>> map = fairBilling.processLines(pList);
		assertEquals(2, map.get("ALICE99").size());
	}

	// This verifies that the Containing Two Start Rows
	@Test
	public void testProcessLineTwoStarts() {
		List<LineInPieces> pList = Arrays.asList(new LineInPieces("14", "02", "11", "ALICE99", "Start"),
				new LineInPieces("14", "02", "12", "ALICE99", "Start"));

		Map<String, List<UserSession>> map = fairBilling.processLines(pList);
		assertEquals(2, map.get("ALICE99").size());
	}

	// This verifies that the Containing Line is Null
	@Test
	public void testProcessLineNull() {
		List<UserResult> results = fairBilling.processFileAsList(null);
		assertEquals(0, results.size());
	}

	// This verifies that the Containing Line is Empty
	@Test
	public void testProcessLineEmpty() {
		List<UserResult> results = fairBilling.processFileAsList(new ArrayList<>());
		assertEquals(0, results.size());
	}

	// This verifies that the Containing Line - Only One Start
	@Test
	public void testProcessLineWithOnlyStart() {
		List<UserResult> results = fairBilling.processFileAsList(Arrays.asList("14:00:00 ALICE99 Start"));
		assertEquals(1, results.size());
		assertEquals("UserResult [userId=ALICE99, numberOfSessions=1, billableTimeInSeconds=0]",
				results.get(0).toString());
	}

	// This verifies that the Containing Line with Two Row Start and End
	@Test
	public void testProcessLineWithStartEnd() {
		List<UserResult> results = fairBilling
				.processFileAsList(Arrays.asList("14:00:00 ALICE99 Start", "14:00:01 ALICE99 End"));
		assertEquals(1, results.size());
		assertEquals("UserResult [userId=ALICE99, numberOfSessions=1, billableTimeInSeconds=1]",
				results.get(0).toString());
	}

	// This verifies that the Containing Line with Three Row (1 Start and 2 End)
	@Test
	public void testProcessLineWithThreeRows() {
		List<UserResult> results = fairBilling.processFileAsList(
				Arrays.asList("14:00:00 ALICE99 Start", "14:00:01 ALICE99 End", "14:00:02 ALICE99 End"));
		assertEquals(1, results.size());
		assertEquals("UserResult [userId=ALICE99, numberOfSessions=2, billableTimeInSeconds=3]",
				results.get(0).toString());
	}

	// This verifies that the first Start is matched with an last End, not the last Start
	@Test
	public void testProcessLineWithAlice() {
		List<UserResult> results = fairBilling.processFileAsList(Arrays.asList("14:02:03 ALICE99 Start",
				"14:02:34 ALICE99 End", "14:02:58 ALICE99 Start", "14:03:33 ALICE99 Start", "14:03:35 ALICE99 End",
				"14:04:05 ALICE99 End", "14:04:23 ALICE99 End"));
		assertEquals(1, results.size());
		assertEquals("UserResult [userId=ALICE99, numberOfSessions=4, billableTimeInSeconds=240]",
				results.get(0).toString());
	}

	// This verifies that the first End is matched has first Start And Last Start is matched Has its Last End
	@Test
	public void testProcessLineWithCHARLIE() {
		List<UserResult> results = fairBilling.processFileAsList(Arrays.asList("14:02:05 CHARLIE End",
				"14:03:02 CHARLIE Start", "14:03:37 CHARLIE End", "14:04:41 CHARLIE Start"));
		assertEquals(1, results.size());
		assertEquals("UserResult [userId=CHARLIE, numberOfSessions=3, billableTimeInSeconds=35]",
				results.get(0).toString());
	}

	// This verifies that the Containing With All Lines are Invalid
	@Test
	public void testProcessLineWithAllLinesInvalid() {
		List<UserResult> results = fairBilling.processFileAsList(Arrays.asList("XXXX", "XXX", "SSS"));
		assertEquals(0, results.size());
	}

}
