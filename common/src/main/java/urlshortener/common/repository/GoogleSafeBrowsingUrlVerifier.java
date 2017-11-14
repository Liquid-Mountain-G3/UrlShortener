package urlshortener.common.repository;

public class GoogleSafeBrowsingUrlVerifier implements UrlVerifier {
    @Override
    public boolean isSafe() {
        return true;
    }
}
