package space.qouve.models;

public enum ServerType {
    PRODUCTIVE("^productive-[a-z0-9-]{1,32}$"),
    STAGING("^staging-[a-z0-9-]{1,32}$"),
    TESTINSTANCE("^testinstance-[a-z0-9-]{1,48}$");

    private final java.util.regex.Pattern idPattern;

    ServerType(String regex) {
        this.idPattern = java.util.regex.Pattern.compile(regex);
    }

    public boolean isValidId(String id) {
        return id != null && idPattern.matcher(id).matches();
    }

    public static ServerType parse(String s) {
        if (s == null) return null;
        try { return valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}