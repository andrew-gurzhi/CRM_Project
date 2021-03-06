package com.ewp.crm.controllers.rest;

import com.ewp.crm.models.*;
import com.ewp.crm.service.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

@RestController
@RequestMapping("/rest/student")
@PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'HR')")
public class StudentRestController {

    private static Logger logger = LoggerFactory.getLogger(StudentRestController.class);

    private final StudentService studentService;

    private final ClientService clientService;

    private final ClientHistoryService clientHistoryService;

    private final StudentStatusService studentStatusService;

    private final ProjectPropertiesService projectPropertiesService;

    private final StatusService statusService;

    private final ClientStatusChangingHistoryService clientStatusChangingHistoryService;

    private final CourseSetService courseSetService;

    @Autowired
    public StudentRestController(StudentService studentService, ClientService clientService,
                                 ClientHistoryService clientHistoryService, StudentStatusService studentStatusService,
                                 ProjectPropertiesService projectPropertiesService, StatusService statusService,
                                 ClientStatusChangingHistoryService clientStatusChangingHistoryService, CourseSetService courseSetService) {
        this.studentService = studentService;
        this.clientService = clientService;
        this.clientHistoryService = clientHistoryService;
        this.studentStatusService = studentStatusService;
        this.projectPropertiesService = projectPropertiesService;
        this.statusService = statusService;
        this.clientStatusChangingHistoryService = clientStatusChangingHistoryService;
        this.courseSetService = courseSetService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable("id") Long id) {
        ResponseEntity result;
        Student student = studentService.get(id);
        if (student != null) {
            result = ResponseEntity.ok(student);
        } else {
            logger.info("Student with id {} not found", id);
            result = new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return result;
    }

    @GetMapping
    public ResponseEntity<Student> getStudentByEmail(@RequestParam("email") String email) {
        ResponseEntity result;
        Student student = studentService.getStudentByEmail(email);
        if (student != null) {
            result = ResponseEntity.ok(student);
        } else {
            logger.info("Student with email {} not found", email);
            result = new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return result;
    }

    @PostMapping("/update")
    public HttpStatus updateStudent(@RequestBody Student student, @AuthenticationPrincipal User userFromSession) {
        if (student.getStatus().getId() == null) {
            studentStatusService.getByName(student.getStatus().getStatus()).ifPresent(student::setStatus);
        }
        Student previous = studentService.get(student.getId());
        Client updatedClient = student.getClient();
        Client client = previous.getClient();
        student.setColor(previous.getColor());
        Optional<ClientHistory> history = clientHistoryService.createStudentUpdateHistory(userFromSession, previous, student, ClientHistory.Type.UPDATE_STUDENT);
        if (history.isPresent()) {
            if (history.get().getLink() != null && !history.get().getLink().isEmpty()) {
                client.addHistory(history.get());
            }
        }
        if (updatedClient.getName() != null && !updatedClient.getName().isEmpty()) {
            client.setName(updatedClient.getName());
        }
        if (updatedClient.getLastName() != null && !updatedClient.getLastName().isEmpty()) {
            client.setLastName(updatedClient.getLastName());
        }
        if (updatedClient.getEmail().isPresent() && !updatedClient.getEmail().get().isEmpty()) {
            client.setEmail(updatedClient.getEmail().get());
        }
        studentService.update(student);
        clientService.updateClient(client);
        return HttpStatus.OK;
    }

    @PostMapping("/delete/{id}")
    public HttpStatus deleteStudent(@PathVariable("id") Long id, @AuthenticationPrincipal User userFromSession) {
        Client client = studentService.get(id).getClient();
        clientHistoryService.createHistory(userFromSession, client, ClientHistory.Type.DELETE_STUDENT).ifPresent(client::addHistory);
        clientService.updateClient(client);
        studentService.delete(id);
        return HttpStatus.OK;
    }

    @PostMapping("/reject/{id}")
    public HttpStatus rejectStudent(@PathVariable("id") Long id, @RequestParam String message, @AuthenticationPrincipal User userFromSession) {
        ProjectProperties properties = projectPropertiesService.getOrCreate();
        Student student = studentService.get(id);
        Client client = student.getClient();
        clientHistoryService.createInfoHistory(userFromSession, client, ClientHistory.Type.DELETE_STUDENT, message).ifPresent(client::addHistory);
        Long statusId = properties.getClientRejectStudentStatus();
        if (statusId != null) {
            Optional<Status> status = statusService.get(statusId);
            if (status.isPresent()) {
                clientService.createClientStatusChangingHistory(client.getStatus(), status.get(), client, false, userFromSession);
                client.setStatus(status.get());
                clientService.updateClient(client);
                return HttpStatus.OK;
            }
        }
        logger.info("Default statuses for rejected students not set!");
        return HttpStatus.CONFLICT;
    }

    @PostMapping("/color/set/{id}")
    public HttpStatus setColor(@PathVariable("id") Long id, @RequestParam String color) {
        Student student = studentService.get(id);
        student.setColor(color);
        studentService.update(student);
        return HttpStatus.OK;
    }

    @PostMapping("/color/reset/{id}")
    public HttpStatus resetColor(@PathVariable("id") Long id) {
        Student student = studentService.get(id);
        student.setColor(null);
        studentService.update(student);
        return HttpStatus.OK;
    }

    @PostMapping("/color/reset/all")
    public HttpStatus resetColors() {
        studentService.resetColors();
        return HttpStatus.OK;
    }

    @GetMapping("/{id}/client")
    public ResponseEntity<Client> getClientByStudentId(@PathVariable("id") Long id) {
        ResponseEntity result;
        Client client = studentService.get(id).getClient();
        if (client != null) {
            result = ResponseEntity.ok(client);
        } else {
            logger.info("Client for student with id {} not found", id);
            result = new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return result;
    }

    @PostMapping("/{id}/notify/email")
    public HttpStatus updateNotifyEmailFlag(@RequestParam boolean status,
                                            @PathVariable("id") Long id,
                                            @AuthenticationPrincipal User userFromSession,
                                            HttpServletRequest request) {
        updateNotification(status, id, userFromSession, request);
        return HttpStatus.OK;
    }

    @PostMapping("/{id}/notify/sms")
    public HttpStatus updateNotifySMSFlag(@RequestParam boolean status,
                                          @PathVariable("id") Long id,
                                          @AuthenticationPrincipal User userFromSession,
                                          HttpServletRequest request) {
        updateNotification(status, id, userFromSession, request);
        return HttpStatus.OK;
    }

    @PostMapping("/{id}/notify/vk")
    public HttpStatus updateNotifyVKFlag(@RequestParam boolean status,
                                         @PathVariable("id") Long id,
                                         @AuthenticationPrincipal User userFromSession,
                                         HttpServletRequest request) {
        updateNotification(status, id, userFromSession, request);
        return HttpStatus.OK;
    }

    /**
     * Change notification status depending on url path.
     *
     * @param status          notification status.
     * @param id              modified student id.
     * @param userFromSession logged in user.
     * @param request         HTTP request.
     */
    private void updateNotification(boolean status, Long id, User userFromSession, HttpServletRequest request) {
        Student previous = studentService.get(id);
        studentService.detach(previous);
        String url = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String key = url.substring(url.lastIndexOf("/") + 1);
        Map<String, Supplier<Student>> filterMap = new HashMap<String, Supplier<Student>>() {
            {
                put("email", () -> {
                    Student current = studentService.get(id);
                    current.setNotifyEmail(status);
                    return current;
                });
                put("sms", () -> {
                    Student current = studentService.get(id);
                    current.setNotifySMS(status);
                    return current;
                });
                put("vk", () -> {
                    Student current = studentService.get(id);
                    current.setNotifyVK(status);
                    return current;
                });
            }
        };
        Student current = filterMap.get(key).get();
        Client client = current.getClient();
        clientHistoryService.createStudentUpdateHistory(userFromSession, client.getStudent(), current, ClientHistory.Type.UPDATE_STUDENT).ifPresent(client::addHistory);
        studentService.update(current);
        clientService.updateClient(client);
    }

    @GetMapping("/all")
    public ResponseEntity getStudentDtoForPageOfAll() {
        if (studentService.getStudentDtoForAllStudentsPage() == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(studentService.getStudentDtoForAllStudentsPage());
    }

    //Запись Студента в Набор
    @PostMapping(value = "/courseSet/add/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'USER','MENTOR','HR')")
    public ResponseEntity studentAddCourseSet(@PathVariable Long clientId,
                                          @RequestParam Long courseSetId) {
        Student student = clientService.getClientByID(clientId).get().getStudent();
        /*Т.к. связь Направление-Студенты один-ко-многим, то сначала нужно удалить из таблицы course_set_students
        запись для студента, если она существует*/
        List<CourseSet> listCourseSet = courseSetService.getAll();
        for (CourseSet cs:listCourseSet) {
            courseSetService.removeFromSetIfContains(cs, student);
        }
        //Теперь можно записать Студента в Набор
        CourseSet courseSet = courseSetService.get(courseSetId);
        Set<Student> set = courseSet.getStudents();
        set.add(student);
        courseSet.setStudents(set);
        courseSetService.update(courseSet);

        return new ResponseEntity(HttpStatus.OK);
    }

    //Получение Набора для Студента
    @GetMapping(value = "/courseSet/get/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'USER','MENTOR','HR')")
    public ResponseEntity<CourseSet> studentGetCourseSet(@PathVariable Long clientId) {
        Student student = clientService.getClientByID(clientId).get().getStudent();
        for (CourseSet cs : courseSetService.getAll()) {
            if (cs.getStudents().contains(student)) {
                return ResponseEntity.ok(cs);
            }
        }
        return null;
    }
}
