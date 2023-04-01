package com.example.had.service;

import com.example.had.entity.*;
import com.example.had.repository.*;
import com.example.had.request.UserProfileUpdateRequest;
import com.example.had.request.updateUserTimestampBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final DoctorConnectionRequestRepository doctorConnectionRequestRepository;

    public UserService(UserRepository userRepository,
                       QuestionRepository questionRepository,
                       AuthRepository authRepository,
                       PasswordEncoder passwordEncoder,
                       DoctorRepository doctorRepository,
                       DoctorConnectionRequestRepository doctorConnectionRequestRepository) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorRepository = doctorRepository;
        this.doctorConnectionRequestRepository = doctorConnectionRequestRepository;
    }

    public ResponseEntity<?> getQuestions(int week, int session) {
        List<Question> questions = questionRepository.findByWeekNumberAndSessionNumber(week, session);
        if (questions.size() == 0){
            System.out.println("Question with week "+week+" session "+session+" not found");
            return ResponseEntity.noContent().build();
        }
        System.out.println("Questions of week "+week+" session "+session+" send");
        return ResponseEntity.ok(questions);
    }

    public User getProfile(UUID patientId) {
        try {
            User user = userRepository.findById(patientId).get();

            System.out.println("Profile of USER "+user.getEmail()+" send");

            return user;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }
    @Transactional
    public boolean updateProfile(UUID patientId, UserProfileUpdateRequest updateRequest) {
        try{
            userRepository.updateAddressAndContactById(
                    updateRequest.getAddress(),
                    updateRequest.getContact(),
                    patientId
            );
            authRepository.updatePasswordByUsername(
                    passwordEncoder.encode(updateRequest.getPassword()),
                    userRepository.findById(patientId).get().getEmail()
            );

            System.out.println("USER "+userRepository.getOne(patientId).getEmail()+" updated profile");

            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean requestDoctor(UUID patienId, UUID doctorId) {
        try{
            User user = userRepository.findById(patienId).get();
            Doctor doctor = doctorRepository.findById(doctorId).get();
            doctorConnectionRequestRepository.save(new DoctorConnectionRequest(user,doctor));

            System.out.println("USER "+user.getEmail()+" requested for DOCTOR "+doctor.getEmail());

            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }
    @Transactional
    public boolean updateTime(updateUserTimestampBody userTimestampBody) {
        try {
            Auth byUsername = authRepository.findByUsername(userTimestampBody.getUsername());
            List<Timestamp> entryTime = byUsername.getEntryTime();
            List<Timestamp> exitTime = byUsername.getExitTime();

            entryTime.add(userTimestampBody.getEntryTime());
            exitTime.add(userTimestampBody.getExitTime());

            byUsername.setEntryTime(entryTime);
            byUsername.setExitTime(exitTime);

            authRepository.save(byUsername);

            System.out.println("USER "+byUsername.getUsername()+" activity accounted");

            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }
}
