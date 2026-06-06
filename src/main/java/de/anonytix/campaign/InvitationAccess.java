package de.anonytix.campaign;

public interface InvitationAccess {

    InvitationDescriptor resolve(String clearToken);

    InvitationDescriptor lock(String clearToken);

    void markUsed(String tokenHash);
}
