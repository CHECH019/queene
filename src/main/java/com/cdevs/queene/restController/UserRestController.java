package com.cdevs.queene.restController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdevs.queene.generics.GenericRestController;
import com.cdevs.queene.generics.GenericServiceApi;
import com.cdevs.queene.global.Constants;
import com.cdevs.queene.model.Appointment;
import com.cdevs.queene.model.Client;
import com.cdevs.queene.model.Employee;
import com.cdevs.queene.model.MyService;
import com.cdevs.queene.model.User;
import com.cdevs.queene.service.api.AppointmentServiceAPI;
import com.cdevs.queene.service.api.MyServiceServiceAPI;
import com.cdevs.queene.service.api.UserServiceAPI;
import com.cdevs.queene.validations.UserValidations;

@RestController
@RequestMapping(value = Constants.BASE_URL)
public class UserRestController extends GenericRestController<User,Long>{
    
    @Autowired
    private UserServiceAPI userService;

    @Autowired
    private AppointmentServiceAPI apService;

    @Autowired
    private MyServiceServiceAPI myServService;

    @RequestMapping("/")
    public String queene(){
        return "Welcome to Queene!";
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody Map<String, Object>userMap) {
        String email = (String) userMap.get("email");
        String pass = (String) userMap.get("password");
        User c = userService.validateUser(email, pass);   
        return new ResponseEntity<Map<String,String>>(UserValidations.generateJWTToken(c), HttpStatus.OK) ;
    }

    @GetMapping("/my-appointments")
    public List<Appointment> myAppts(HttpServletRequest request, Model model){
        long id = (long) request.getAttribute("userID");
        String role = (String) request.getAttribute("role");

        return role.equals(Constants.ROLE_EMPLOYEE) ? 
                apService.getByEmployeeId(id) : apService.getByClientId(id);
    }

    @PostMapping("/book")
    public ResponseEntity<Appointment> saveAppointment(@RequestBody Appointment a, HttpServletRequest request){
        if(!request.getAttribute("role").equals(Constants.ROLE_CLIENT)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(a.getEmployee() == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        User employee = userService.get(a.getEmployee().getId());
        if(employee != null && !employee.getUserRole().equals(Constants.ROLE_EMPLOYEE)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        long id = (long) request.getAttribute("userID");
        User client = userService.get(id);
        List<MyService> myServices = new ArrayList<>();
        a.getServices().forEach(serv -> myServices.add(myServService.get(serv.getId())));
        a.setClient((Client) client);
        a.setEmployee((Employee) employee);
        a.setServices(myServices);
        apService.save(a);
        return new ResponseEntity<Appointment>(a, HttpStatus.OK);    
        
    }

    @PostMapping("save/client")
    public ResponseEntity<Client> save(@RequestBody Client entity){
        entity.setUserRole(Constants.ROLE_CLIENT);
        Client e = (Client) userService.save(entity); 
        return new ResponseEntity<>(e, HttpStatus.OK);
    }

    @PostMapping("save/employee")
    public ResponseEntity<Employee> saveEmp(@RequestBody Employee entity){
        entity.setUserRole(Constants.ROLE_EMPLOYEE);
        Employee e = (Employee) userService.save(entity); 
        return new ResponseEntity<>(e, HttpStatus.OK);
    }
    @GetMapping("/all-clients")
    public ResponseEntity<List<User>> getAllClients(Model model, HttpServletRequest request){
        String role = (String) request.getAttribute("role");
        if(role.equals(Constants.ROLE_ADMIN)){
            return new ResponseEntity<>(userService.getByRole("Client"),HttpStatus.OK);
        }
        return new ResponseEntity<>(null,HttpStatus.FORBIDDEN);
    }
    @GetMapping("/all-employees")
    public ResponseEntity<List<User>> getAllEmp(Model model, HttpServletRequest request){
        String role = (String) request.getAttribute("role");
        if(role.equals(Constants.ROLE_ADMIN)){
            return new ResponseEntity<>(userService.getByRole("Employee"),HttpStatus.OK);
        }
        return new ResponseEntity<>(null,HttpStatus.FORBIDDEN);
    }

    @GetMapping("/employees-by-services")
    public ResponseEntity<List<User>> getByServices(@RequestBody List<MyService> services){
        services.forEach(serv -> System.out.println(serv.getId()));
        return new ResponseEntity<>(userService.getbyServicesList(services),HttpStatus.OK);
    }
    @Override
    public GenericServiceApi<User, Long> getService() {
        return userService;
    }
}