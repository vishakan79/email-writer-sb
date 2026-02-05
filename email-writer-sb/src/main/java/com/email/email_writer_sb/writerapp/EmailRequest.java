package com.email.email_writer_sb.writerapp;

import lombok.Data;

@Data
public class EmailRequest {
    private String emailcontent; // which type  content content you need;
    private String tone; // tone is meaning the spending email like professional, funny, friendly;
}
