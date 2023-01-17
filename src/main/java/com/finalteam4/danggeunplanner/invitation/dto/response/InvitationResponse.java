package com.finalteam4.danggeunplanner.invitation.dto.response;

import com.finalteam4.danggeunplanner.member.entity.Member;
import lombok.Getter;

@Getter
public class InvitationResponse {
    private final String username;

    public InvitationResponse(Member member){
        this.username = member.getUsername();
    }
}
