package com.example.ProyectoTingeso.Controllers;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Services.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers") // Ajusta la ruta base según tu preferencia
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:8008",
        "http://localhost:8070",
        "http://localhost:3000"
})
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * RF 3.1
     */
    @PostMapping("/registerCustomer")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> registerCustomer(@RequestBody Map<String, Object> customerPayload) {
        try {
            String name = (String) customerPayload.get("name");
            String phoneNumber = (String) customerPayload.get("phoneNumber");
            String email = (String) customerPayload.get("email");
            String rut = (String) customerPayload.get("rut");


            CustomerEntity saved = customerService.registerCustomer(name, phoneNumber, email, rut);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al registrar cliente: " + e.getMessage());
        }
    }

    /**
     *
     */
    @GetMapping("/getAll")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getAllCustomers() {
        try {
            List<CustomerEntity> customers = customerService.getAllCustomer();
            return ResponseEntity.ok(customers);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la lista de clientes: " + e.getMessage());
        }
    }

    /**
     * Verificar estado del cliente (Activo/Restringido)
     */
    @GetMapping("/state/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getStateCustomer(@PathVariable Long id) {
        try {
            Boolean isActive = customerService.getStateCustomer(id);
            return ResponseEntity.ok(isActive);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar estado: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> countCustomer (){
        try {
            int number = Math.toIntExact(customerService.countAllCustomer());
            return ResponseEntity.ok(number);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en entregar la información " + e.getMessage());
        }
    }

    @GetMapping("/countActive")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> countActiveCustomer (){
        try {
            int number = customerService.countAllByActive();
            return ResponseEntity.ok(number);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en entregar la información " + e.getMessage());
        }
    }




}