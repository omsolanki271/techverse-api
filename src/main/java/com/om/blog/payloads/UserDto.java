package com.om.blog.payloads;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private int id;
    private  String name;
    private  String email;
    private  String password;
    private String about;
}
