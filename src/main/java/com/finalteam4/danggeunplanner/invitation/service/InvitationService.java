package com.finalteam4.danggeunplanner.invitation.service;

import com.finalteam4.danggeunplanner.common.exception.DanggeunPlannerException;
import com.finalteam4.danggeunplanner.group.entity.Group;
import com.finalteam4.danggeunplanner.group.repository.GroupRepository;
import com.finalteam4.danggeunplanner.invitation.dto.response.InvitationListResponse;
import com.finalteam4.danggeunplanner.invitation.dto.response.InvitationResponse;
import com.finalteam4.danggeunplanner.invitation.entity.Invitation;
import com.finalteam4.danggeunplanner.invitation.repository.InvitationRepository;
import com.finalteam4.danggeunplanner.member.entity.Member;
import com.finalteam4.danggeunplanner.member.repository.MemberRepository;
import com.finalteam4.danggeunplanner.member.service.MemberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.finalteam4.danggeunplanner.common.exception.ErrorCode.NOT_FOUND_GROUP;
import static com.finalteam4.danggeunplanner.common.exception.ErrorCode.NOT_FOUND_INVITATION;
import static com.finalteam4.danggeunplanner.common.exception.ErrorCode.NOT_FOUND_MEMBER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationService {
    private final InvitationRepository invitationRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final MemberValidator memberValidator;
    private final InvitationValidator invitationValidator;
    @Transactional
    public void create(Member member,Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(
                ()->new DanggeunPlannerException(NOT_FOUND_GROUP)
        );
        invitationValidator.validateAdmin(member,group);
        deleteExistInvitation(member);

        Invitation invitation = new Invitation(group);
        invitationRepository.save(invitation);
        member.confirmInvitation(invitation);
    }

    public InvitationListResponse find(Member member,Long groupId) {
        Invitation invitation = invitationRepository.findById(member.getInvitation().getId()).orElseThrow(
                ()->new DanggeunPlannerException(NOT_FOUND_INVITATION)
        );
        Group group = groupRepository.findById(groupId).orElseThrow(
                ()->new DanggeunPlannerException(NOT_FOUND_GROUP)
        );
        invitationValidator.validateAdmin(member,group);

        InvitationListResponse response = new InvitationListResponse();

        for(Member other : invitation.getMembers()){
            response.addInvitation(new InvitationResponse(other));
        }

        return response;
    }
    @Transactional
    public InvitationResponse addMember(Member member,Long groupId, String username){
        Invitation invitation = invitationRepository.findById(member.getInvitation().getId()).orElseThrow(
                ()->new DanggeunPlannerException(NOT_FOUND_INVITATION)
        );
        Member other = memberRepository.findByUsername(username).orElseThrow(
                ()->new DanggeunPlannerException(NOT_FOUND_MEMBER)
        );
        Group group = groupRepository.findById(groupId).orElseThrow(
                ()->new DanggeunPlannerException(NOT_FOUND_GROUP)
        );

        invitationValidator.validateAdmin(member,group);
        invitationValidator.validateMaxSize(invitation,group);
        invitationValidator.validateParticipant(group,other);
        invitationValidator.validateInvitedMember(invitation,other);

        invitation.addMember(other);

        return new InvitationResponse(other);
    }
    @Transactional
    public InvitationResponse removeMember(Member member, Long groupId, String username) {
        Invitation invitation = invitationRepository.findById(member.getInvitation().getId()).orElseThrow(
                ()->new DanggeunPlannerException(NOT_FOUND_INVITATION)
        );
        Member other = memberRepository.findByUsername(username).orElseThrow(
                ()->new DanggeunPlannerException(NOT_FOUND_MEMBER)
        );
        Group group = groupRepository.findById(groupId).orElseThrow(
                ()->new DanggeunPlannerException(NOT_FOUND_GROUP)
        );

        invitationValidator.validateAdmin(member,group);
        invitationValidator.validateExistMember(invitation,other);

        invitation.removeMember(other);

        return new InvitationResponse(other);
    }
    private void deleteExistInvitation(Member member){
        if(memberRepository.existsInvitationByUsername(member)){
            member.deleteInvitation();
        }
    }
}