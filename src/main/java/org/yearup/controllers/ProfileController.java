package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Profile;
import org.yearup.models.User;
import org.yearup.service.ProfileService;
import org.yearup.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("profile")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class ProfileController
{
    private final ProfileService profileService;
    private final UserService userService;

    public ProfileController(ProfileService profileService, UserService userService)
    {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("")
    public Profile getProfile(Principal principal)
    {
        int userId = getUserId(principal);
        Profile profile = profileService.getByUserId(userId);

        if (profile == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return profile;
    }

    @PutMapping("")
    public Profile updateProfile(Principal principal, @RequestBody Profile profile)
    {
        int userId = getUserId(principal);
        return profileService.update(userId, profile);
    }

    private int getUserId(Principal principal)
    {
        User user = userService.getByUserName(principal.getName());
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return user.getId();
    }
}
