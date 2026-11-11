package oversecured.ovaa.services;

interface ICredentialsService {
    String getPassword();

    String readFile(String path);

    void storeToken(String token);
}
