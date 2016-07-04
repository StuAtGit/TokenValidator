##Token Validation Library
Small library, that accepts a validation resource, and expects back 200 or not 200 responses. 
It also attempts to check if the resource returns a JSON entity with the fields:
 
 - expires_in
 - expiration
 
 If it does, it caches the response. If the library detects the resource does not return JSON,
 it stops trying to parse & cache the responses.
 
 Currently this just works with one token validation resource. Might need to look at supporting multiple.
 Or should clients keep multiple validators around?