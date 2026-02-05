package com.email.email_writer_sb.writerapp;



import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class EmailGenertorController {
    private final EmailGenertorService emailGenertorService;
   @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest){
       String response =emailGenertorService.generateEmailReply(emailRequest);
        return  ResponseEntity.ok(response);
    }
}
