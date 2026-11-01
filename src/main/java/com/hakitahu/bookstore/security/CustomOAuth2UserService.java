package com.hakitahu.bookstore.security;

import com.hakitahu.bookstore.entity.User;
import com.hakitahu.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub"); 

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email); 
            newUser.setFullName(name);
            newUser.setProvider("GOOGLE");
            newUser.setProviderId(providerId);
            newUser.setIsEmailVerified(true); // Mặc định Google đã xác thực
            newUser.setPassword(""); 
            userRepository.save(newUser);
        }
        return oAuth2User;
    }
}