package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.EditPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // loads only UserController not entire application context
import org.springframework.boot.test.mock.mockito.MockBean; // mocks UserService so real database is not managed
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // used to define content type as JSON
import org.springframework.test.web.servlet.MockMvc; // mocks HTTP requests and responses
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize; // used for asserting JSON values
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given; // sets expectations in mock test
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired // injects the MockMvc object automatically
  private MockMvc mockMvc;

  @MockBean // mocks the UserService to not make real calls to database
  private UserService userService;

  @MockBean // mocks the UserRepository
  private UserRepository userRepository;

  @Test // marks as test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given setup data
    User user = new User();
    user.setId(1L);
    user.setUsername("firstname@lastname");
    user.setCreationDate("03.03.2025");
    user.setStatus(UserStatus.OFFLINE);
    user.setBirthday("01.01.2000");

    List<User> allUsers = Collections.singletonList(user); // list containing above user

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers); // mocks getUsers() method to return above list

    // when (simulating GET request to /users with response in JSON format
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then (assertions on response)
    mockMvc.perform(getRequest).andExpect(status().isOk())
            // response is an array [0] accesses first (and only) element of array
        .andExpect(jsonPath("$", hasSize(1))) // list has only one user
        .andExpect(jsonPath("$[0].id", is(user.getId().intValue())))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
            .andExpect(jsonPath("$[0].creationDate", is(user.getCreationDate())))
            .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())))
            .andExpect(jsonPath("$[0].birthday", is(user.getBirthday())));

  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setPassword("testPassword");
    user.setCreationDate("03.03.2025");
    user.setBirthday("01.01.2000");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("testPassword");

    given(userService.createUser(any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON) // response as a JSON
        .content(asJsonString(userPostDTO)); // it gets a body as a JSON

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
            .andExpect(jsonPath("$.creationDate", is(user.getCreationDate())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
            .andExpect(jsonPath("$.birthday", is(user.getBirthday())));
  }

  @Test
  public void createUser_invalidInput_thenReturn409Error() throws Exception {
      // given
      User existingUser = new User();
      existingUser.setUsername("existingUser");
      existingUser.setPassword("password123");

      UserPostDTO newUser = new UserPostDTO();
      newUser.setUsername("existingUser");
      newUser.setPassword("newpassword");

      // mock createUser() to throw 409 Error
      given(userService.createUser(any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "The username provided is not unique. Therefore, the user could not be created!"));

      // when
      MockHttpServletRequestBuilder postRequest = post("/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(newUser));
      //then
      mockMvc.perform(postRequest)
              .andExpect(status().isConflict())
              .andExpect(status().reason(is("The username provided is not unique. Therefore, the user could not be created!")));
  }

  @Test
  public void givenUserId_whenGetUserById_thenReturnJson() throws Exception {
      //given
      User user = new User();
      user.setId(1L);
      user.setUsername("testUsername");
      user.setCreationDate("03.03.2025");
      user.setStatus(UserStatus.ONLINE);
      user.setBirthday("01.01.2000");

      // mock
      given(userService.authenticateUser(anyString())).willReturn(true);

      given(userService.getUserById(anyLong())).willReturn(user); //

      //when
      MockHttpServletRequestBuilder getRequest = get("/users/1")
              .header("Authorization", "Bearer testToken") // mock authorization header
              .contentType(MediaType.APPLICATION_JSON); // response in JSON
      //then
      mockMvc.perform(getRequest)
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id", is(user.getId().intValue())))
              .andExpect(jsonPath("$.username", is(user.getUsername())))
              .andExpect(jsonPath("$.creationDate", is(user.getCreationDate())))
              .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
              .andExpect(jsonPath("$.birthday", is(user.getBirthday())));

  }

  @Test
  public void givenNonExistentUserId_whenGetUserById_thenReturn404Error() throws Exception {
      // given
      long nonExistentUserId = 999L; // ID that does not exist in the mock data

      // mock
      given(userService.authenticateUser(anyString())).willReturn(true);

      given(userService.getUserById(nonExistentUserId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

      // when
      MockHttpServletRequestBuilder getRequest = get("/users/{userId}", nonExistentUserId)
              .header("Authorization", "Bearer testToken") // mock authorization header
              .contentType(MediaType.APPLICATION_JSON);

      // then
      mockMvc.perform(getRequest)
              .andExpect(status().isNotFound()) // status 404
              .andExpect(status().reason(is("User not found"))); // reason for the error
  }

  @Test
  public void givenValidUserId_whenUpdateUser_thenReturnNoContent() throws Exception {
      // given
      long validUserId = 1L;  // existing user ID
      User existingUser = new User();
      existingUser.setId(validUserId);
      existingUser.setUsername("oldUsername");
      existingUser.setBirthday("01.01.1990");
      existingUser.setCreationDate("01.01.2020");
      existingUser.setStatus(UserStatus.ONLINE);

      // Updated user profile data
      EditPutDTO updatedUser = new EditPutDTO();
      updatedUser.setUsername("newUsername");
      updatedUser.setBirthday("02.02.1992");

      // mock
      given(userService.getUserById(validUserId)).willReturn(existingUser);

      // mock
      doNothing().when(userService).update(eq(validUserId), any(EditPutDTO.class)); // simulate void return type

      // when
      MockHttpServletRequestBuilder putRequest = put("/users/{id}", validUserId)
              .contentType(MediaType.APPLICATION_JSON)  // content type as JSON
              .content(asJsonString(updatedUser));  // the updated user data as JSON

      // then
      mockMvc.perform(putRequest)
              .andExpect(status().isNoContent())  // should return 204 No Content
              .andExpect(content().string(is("")));  // body should be empty for 204 response
  }

  @Test
  public void updateUser_whenUserNotFound_thenReturn404() throws Exception {
      // given
      long nonExistingUserId = 999L; // An ID that doesn't exist in the database
      EditPutDTO editPutDTO = new EditPutDTO();
      editPutDTO.setUsername("newUsername");
      editPutDTO.setBirthday("01.01.2000");

      // mock
      doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
              .when(userService).update(eq(nonExistingUserId), any(EditPutDTO.class)); // cant use given as update returns nothing (void)


      // when
      MockHttpServletRequestBuilder putRequest = put("/users/{userId}", nonExistingUserId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(editPutDTO)); // Convert DTO to JSON

      // then
      mockMvc.perform(putRequest)
              .andExpect(status().isNotFound()) // Expect a 404 status code
              .andExpect(status().reason(is("User not found"))); // Expect the error message
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  // converts object to JSON
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}