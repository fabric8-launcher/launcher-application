package io.fabric8.launcher.base.test.identity;

public final class TokenFixtures {

    private TokenFixtures() {
    }

    /*
        Keys generated using:
        $ ssh-keygen -f auth_rsa
        $ ssh-keygen -e -m PKCS8 -f auth_rsa.pub > auth_rsa_pkcs8.pub

        Tokens generated using https://github.com/clarketm/jwt-cli
        $ jwt sign --algorithm RS256 --keyid "test_id" --issuer "osio"  --expiresIn '50y' -- '{"user": "OSIO"}' "$(cat auth_rsa)" > token
        $ jwt verify --algorithms RS256 -- "$(cat token)" "$(cat auth_rsa_pkcs8.pub)"
    */

    public static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqoh1kM427YUrpKUl6mSv\n" +
            "EQ1ZoBmYXVXISQX+wO/yAahQRf1FqoGGBJ3NGo3Hto+Z3JVXxh0gNnmEhE38ODRB\n" +
            "nW8MQXSNzF962nCcMsQwbDv4DAZetO47bQ7bxIcyR/erPNuQXkUo7VbEbk1+klIL\n" +
            "hrJgyGaD9s2Prs+PaJmUBGHILlZlyz8o8SBbz++gYhMhYGKWXYGpOWxlGBJUWJsZ\n" +
            "fQNA7fid+00tM0ne7TEbXY3EInDjOjf36gb4orfpSmlxFKM31jOsjSC5RB4boHcz\n" +
            "L4/2phzAiC+XfVaIkuMO8PTGxpM9B6wc9nOPf9qTy6G/uwcC0SrnDaANKNb1vO85\n" +
            "PwIDAQAB\n" +
            "-----END PUBLIC KEY-----\n";

    public static final String STRIP_PUBLIC_KEY = stripHeaderAndFooter(PUBLIC_KEY);

    public static final String KID = "test_id";

    public static final String VALID_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3RfaWQifQ.eyJ1c2VyIjoiT1NJTyIsImlhd" +
            "CI6MTUzOTM1OTU0OSwiZXhwIjozMTE3MjM5NTQ5LCJpc3MiOiJvc2lvIn0.kXEZzVbTw8O5Tq4cKqRuQN556F4cK7o5OjOl7OE7YgGQUlsc1JL" +
            "_ZOpagDBgQqF3IKqMlARLN6ObmzEkHorTWgM_GRYHH6-uDTVBQVUgz25ZnAv_e33ckGl1qO48fNt1AyMLbz269-7PAZTUA7arCWGjxkgVCMZWMz" +
            "kIYoHR8Zaj5_hUU74MJSVVfybCvQAYU1gIyqvsJpEPUm3nUV1Ss4B0T_g0gheCgSSrr4-3_3krr09y50KSgroXD2k_ypcUpGt_Wc9-HcRM3NqsI" +
            "ytDvwjmC_ex3TTaPckD3VmdoffUjMNfHsNUNgyAj7MOG-YlSzv5MHTTXaXnel2GEFivOw";

    public static final String OUTDATED_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3RfaWQifQ.eyJ1c2VyIjoiT1NJTyIsIml" +
            "hdCI6MTUzOTM2MDAyOCwiZXhwIjoxNTM5MzYwMDI5LCJpc3MiOiJvc2lvIn0.SG99iSgaiGzK7OZFw9skbVin_ZQqcT5XJ6hNu7lnj5shnB-nLI" +
            "6oBt56zd08wGhXRWA728QnDwMdkhkgnSGbdV7RSxIYY-AYHbaneMGjmffxUiNjLpM13pJyQw_RsVtw9q-WFIPYP2umKBnj2HN-BWMsnND8Q0wHFU" +
            "Z0oRdhSZUE8uQJt_o6K8SJfmRQR1CbMdOlM6nZNXDNoQqUovqaMKns7o8jq0GOjxk92s4xvrZfMqx9BcyhMZ6ycvI9DLLvK1B2uZBLz-iMP-R-rG" +
            "B5tLI9WbQIdKh4cXqZdKoi8d9DIU9FJmW38jXcdfhQ44uq6MpEKzUq3OVO59uk5jWANA";

    public static final String TOKEN_SIGNED_WITH_DIFFERENT_KEY = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3RfaWQifQ.eyJ1c2Vy" +
            "IjoiT1NJTyIsImlhdCI6MTUzOTM2MDA3OSwiZXhwIjozMTE3MjQwMDc5LCJpc3MiOiJvc2lvIn0.P5KwJwBbyWqMgsKaBb__xXHbZ_yUkGTH8SQ5Z" +
            "oqykNcXv6Vc767RHJBSwLBLiw4wFtXU-3-jA_Fzf9NcAW5uoiWIUOL9k_s6OlwD0onlzzkfWyakUZdSxnc5WabE699c2TRSh8fsEJ6mcj83qbdNsE" +
            "cx90t1CU5bFDRIIt4H_GVZaV5l25OlhWAaJuHTy3T6uaQUm26KcxI5UF_5ZPTlybjyE8aTPgDKCrNZcsM9rNNW4dOsj8W5e6-rUPVTdO0MSnVc1EM" +
            "djcIqI3bUn2t-qJhrQwmQvTQUJuqMVQmb97F4-llIaQ3FLj0-Hv9Gj7QTTiJc3NpzFZI2zXe2chMp4A";

    private static String stripHeaderAndFooter(String key) {
        return key.replaceAll("\\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");
    }
}
