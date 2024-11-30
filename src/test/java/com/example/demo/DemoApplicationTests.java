package com.example.demo;

import com.example.demo.dto.AnswerDTO;
import com.example.demo.dto.ArtifactDTO;
import com.example.demo.dto.CourseRegisterDTO;
import com.example.demo.dto.EvaluateAnswerDTO;
import com.example.demo.dto.GetAnswersDTO;
import com.example.demo.dto.GetArtifactDTO;
import com.example.demo.dto.GetMessageDTO;
import com.example.demo.dto.GetRatingDTO;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.MessageDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.dto.UpdateCourseDTO;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.enums.AnswerStatus;
import com.example.demo.enums.AnswerType;
import com.example.demo.enums.ArtifactType;
import com.example.demo.enums.Role;
import com.example.demo.model.Answer;
import com.example.demo.model.Artifact;
import com.example.demo.model.Course;
import com.example.demo.model.Grade;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.ArtifactRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ArtifactService;
import com.example.demo.service.FileService;
import com.example.demo.service.JwtTokenProvider;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.Filter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.MOCK,
		classes = Main.class)
@AutoConfigureMockMvc
public class DemoApplicationTests {
	@Autowired
	private WebApplicationContext webApplicationContext;
	@Autowired
	private Filter springSecurityFilterChain;
	private MockMvc mvc;
	private final ObjectMapper mapper = new ObjectMapper();
	@MockBean
	private UserRepository userRepository;
	@MockBean
	private CourseRepository courseRepository;
	@MockBean
	private ArtifactRepository artifactRepository;
	@MockBean
	private AnswerRepository answerRepository;
	@Autowired
	private UserService userService;
	@MockBean
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	@Autowired
	private ArtifactService artifactService;
	@MockBean
	private FileService fileService;
	@MockBean
	private GradeRepository gradeRepository;
	@MockBean
	private MessageRepository messageRepository;

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.addFilters(springSecurityFilterChain).build();
		mapper.registerModule(new JavaTimeModule());
	}

	@Test
	public void registerTest() throws Exception {
		RegisterDTO dto = new RegisterDTO();
		dto.setEmail("test@test.ru");
		dto.setPassword("1234");
		dto.setFirst_name("Alex");
		dto.setSecond_name("Nik");

		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(7);
			return user;
		});

		mvc.perform(post("/register")
						.content(mapper.writeValueAsString(dto))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	public void loginTest() throws Exception {
		LoginDTO dto = createLoginDTO();

		User user = createUser("STUDENT");

		Authentication auth = new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword(), List.of(new SimpleGrantedAuthority("STUDENT")));
		when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(userService.loadUserByUsername(user.getUsername())).thenAnswer(invocationOnMock -> Optional.of(user));


		mvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(dto)))
				.andExpect(status().isOk()).andReturn().getResponse();
	}

	@Test
	public void profileUpdateTest() throws Exception {
		UserProfileDTO dto = new UserProfileDTO();
		dto.setPassword("12345");

		User user = createUser("STUDENT");

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> user);

		mvc.perform(put("/profile/update")
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(dto)))
				.andExpect(status().isOk()).andReturn().getResponse();
	}

	@Test
	public void addAnswerTest() throws Exception {
		AnswerDTO dto = new AnswerDTO();
		dto.setAnswerType(AnswerType.TEXT);
		dto.setContent("TEST");
		dto.setArtifactId(1);
		dto.setCourseId(1);

		User user = createUser("STUDENT");
		Course course = new Course();
		Artifact artifact = new Artifact();
		user.setCourses(List.of(course));
		course.setId(1);
		course.setStudents(List.of(user));
		artifact.setCourse(course);
		artifact.setArtifactType(ArtifactType.TASK);

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(userRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(user));
		when(courseRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(course));
		when(artifactRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(artifact));
		when(answerRepository.save(any(Answer.class))).thenAnswer(invocationOnMock -> new Answer());

		MockMultipartFile jsonFile = new MockMultipartFile(
				"dto",
				"",
				"application/json",
				mapper.writeValueAsBytes(dto)
		);

		mvc.perform(multipart("/answers/add")
						.file(jsonFile)
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isCreated()).andReturn().getResponse();
	}

	@Test
	public void getAnswersTest() throws Exception {
		GetAnswersDTO dto = new GetAnswersDTO();
		dto.setCourseId(1);
		dto.setStatus(AnswerStatus.UNCHECKED);

		User user = createUser("TEACHER");
		Course course = new Course();
		course.setTeacher(user);

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(userRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(user));
		when(courseRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(course));
		when(answerRepository.findByAnswerTypeAndCourseIdAndArtifactType(any(), anyInt(), any()))
				.thenAnswer(invocationOnMock -> Collections.emptyList());

		mvc.perform(post("/answers/getAnswers")
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(dto)))
				.andExpect(status().isOk()).andReturn().getResponse();
	}

	@Test
	public void addArtifactTest() throws Exception {
		ArtifactDTO dto = new ArtifactDTO();
		dto.setContent("TEST");
		dto.setTitle("TEST");
		dto.setArtifactType(ArtifactType.TASK);
		dto.setCourseId(1);

		User user = createUser("TEACHER");
		Course course = new Course();
		user.setCourses(List.of(course));
		course.setId(1);
		course.setTeacher(user);

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(userRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(user));
		when(courseRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(course));
		when(artifactRepository.save(any(Artifact.class))).thenAnswer(invocationOnMock -> new Artifact());

		MockMultipartFile jsonFile = new MockMultipartFile(
				"dto",
				"",
				"application/json",
				mapper.writeValueAsBytes(dto)
		);

		mvc.perform(multipart("/artifact/add")
						.file(jsonFile)
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isCreated()).andReturn().getResponse();
	}

	@Test
	public void getArtifactsTest() throws Exception {
		GetArtifactDTO dto = new GetArtifactDTO();
		dto.setCourseId(1);

		User user = createUser("TEACHER");
		Course course = new Course();
		course.setId(1);
		course.setTeacher(user);
		Artifact artifact = Artifact.builder()
					.id(1)
					.artifactType(ArtifactType.TASK)
					.title("TEST")
					.course(course)
					.content("TEST")
					.build();

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(artifactRepository.findAll(PageRequest.of(0, 10)))
				.thenAnswer(invocationOnMock -> new PageImpl<>(List.of(artifact)));
		when(artifactService.getArtifactByTypeAndCourseId(dto, PageRequest.of(0, 10)))
				.thenAnswer(invocationOnMock -> new PageImpl<>(Collections.emptyList()));

		mvc.perform(post("/artifact/getArtifacts")
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(dto)))
				.andExpect(status().is5xxServerError()).andReturn().getResponse();
	}

	@Test
	public void updateCourseTest() throws Exception {
		UpdateCourseDTO dto = new UpdateCourseDTO();
		dto.setCourse_id(1);
		dto.setTitle("TEST");

		User user = createUser("TEACHER");
		Course course = new Course();
		course.setId(1);
		course.setTeacher(user);

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(userRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(user));
		when(courseRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(course));
		when(courseRepository.save(any(Course.class))).thenAnswer(invocationOnMock -> course);

		mvc.perform(put("/course/update")
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(dto)))
				.andExpect(status().isOk()).andReturn().getResponse();
	}

	@Test
	public void getAllCourseTest() throws Exception {
		User user = createUser("TEACHER");
		Course course = new Course();
		course.setId(1);
		course.setTeacher(user);

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(courseRepository.findAll(PageRequest.of(0, 10)))
								.thenAnswer(invocationOnMock -> new PageImpl<>(List.of(course)));

		mvc.perform(get("/course/all")
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is5xxServerError()).andReturn().getResponse();
	}

	@Test
	public void registerToCourseTest() throws Exception {
		CourseRegisterDTO dto = new CourseRegisterDTO();
		dto.setCourseId(1);

		User user = createUser("STUDENT");
		Course course = new Course();
		course.setId(1);
		course.setTeacher(user);
		course.setStudents(new ArrayList<>());

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(userRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(user));
		when(courseRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(course));
		when(courseRepository.save(any())).thenAnswer(invocationOnMock -> course);

		mvc.perform(post("/course/register")
						.content(mapper.writeValueAsString(dto))
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated()).andReturn().getResponse();
	}


	@Test
	public void evaluateAnswerTest() throws Exception {
		EvaluateAnswerDTO dto = new EvaluateAnswerDTO();
		dto.setAnswerId(1);
		dto.setArtifactId(1);
		dto.setCourseId(1);
		dto.setGrade(5);

		User user = createUser("TEACHER");
		Course course = new Course();
		Artifact artifact = new Artifact();
		Answer answer = new Answer();

		course.setId(1);
		course.setTeacher(user);
		course.setStudents(new ArrayList<>());

		artifact.setCourse(course);
		artifact.setArtifactType(ArtifactType.TASK);

		answer.setCourse(course);
		answer.setArtifact(artifact);

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(courseRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(course));
		when(artifactRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(artifact));
		when(answerRepository.findById(anyInt())).thenAnswer(invocationOnMock -> Optional.of(answer));
		when(gradeRepository.save(any())).thenAnswer(invocationOnMock -> new Grade());
		when(answerRepository.save(any())).thenAnswer(invocationOnMock -> answer);

		mvc.perform(post("/grades/evaluate")
						.content(mapper.writeValueAsString(dto))
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated()).andReturn().getResponse();
	}

	@Test
	public void getRatingTest() throws Exception {
		GetRatingDTO dto = new GetRatingDTO();
		dto.setCourseId(1);

		User user = createUser("TEACHER");
		Course course = new Course();
		course.setId(1);
		course.setTeacher(user);
		course.setStudents(new ArrayList<>());

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(user));
		when(gradeRepository.findRatingByCourseId(anyInt())).thenAnswer(invocationOnMock -> Collections.emptyList());

		mvc.perform(post("/grades/getRating")
						.content(mapper.writeValueAsString(dto))
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(user))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse();
	}

	@Test
	public void sendMessageTest() throws Exception {
		MessageDTO dto = new MessageDTO();
		dto.setSenderId(1);
		dto.setRecipientId(2);
		dto.setContent("Hello!");

		User sender = createUser("STUDENT");
		User recipient = createUser("TEACHER");

		sender.setId(1);
		recipient.setId(2);

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(sender));
		when(userRepository.findById(1)).thenAnswer(invocationOnMock -> Optional.of(sender));
		when(userRepository.findById(2)).thenAnswer(invocationOnMock -> Optional.of(recipient));
		when(messageRepository.save(any())).thenAnswer(invocationOnMock -> new Message());

		mvc.perform(post("/messages/send")
						.content(mapper.writeValueAsString(dto))
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(sender))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated()).andReturn().getResponse();
	}

	@Test
	public void getMessagesTest() throws Exception {
		GetMessageDTO dto = new GetMessageDTO();
		dto.setRecipientId(2);

		User sender = createUser("STUDENT");
		User recipient = createUser("TEACHER");

		sender.setId(1);
		recipient.setId(2);

		when(userRepository.findByEmail(any(String.class))).thenAnswer(invocationOnMock -> Optional.of(sender));
		when(messageRepository.findBySenderIdAndRecipientId(anyInt(), anyInt(), any(PageRequest.class)))
				.thenAnswer(invocationOnMock -> new PageImpl<>(new ArrayList<>()));

		mvc.perform(post("/messages/get_msgs")
						.content(mapper.writeValueAsString(dto))
						.header("Authorization", "Bearer " + jwtTokenProvider.generateToken(sender))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is5xxServerError()).andReturn().getResponse();
	}

	private LoginDTO createLoginDTO() {
		LoginDTO loginDTO = new LoginDTO();

		loginDTO.setEmail("test@test.ru");
		loginDTO.setPassword("1234");

		return loginDTO;
	}

	private User createUser(String role) {
		return User.builder()
				.id(7)
				.email("test@test.ru")
				.password("1234")
				.role(Role.valueOf(role))
				.build();
	}
}
