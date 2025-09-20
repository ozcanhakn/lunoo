package com.lumoo.FCM;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {
    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";


    public String getAccessToken(){
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"lunoo-78c1d\",\n" +
                    "  \"private_key_id\": \"916419b18f65cc88a8b09c117b75c395d8b4347d\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7IxpC/uRy08WQ\\nfDP4aDxlsDMBU6IWI5FALQ7MhjBH1/7NyzTjDoCu1yOLTokzZISMeL814q7cDZWt\\nkNz5ZSzYAmcs++t7dw8gNbvvMYvUl878B52HoMSvYi/oaGhKB/MPKzfqh3rm9W8C\\nT7C7hVHQx9NpIKP8THbjqIY9KBmPwW03LEWlXt8x+lcp6qFME/uq+oJNEFztel/7\\nYTLpkeg2O5L9tHCNwlj8z3K4h2sPfCxgjnvEWAkhOqnzbLgES3J43mShq+jWHa+9\\nLkQ3/eoQUvjYXdp1fV0NaetSQXj4omDKFb4lrNnxolvcJlo88AA0rRWRyrr2y6Sh\\n/1zEBs+HAgMBAAECggEAE0tSONgY67Hj67qyjQkKOR3KlHdv/cJbBnfez72CGtYt\\nQkCSSlrDb8Nkucc2JCNX+zez1zVw++U2qneGIPyKfBb7Vkr/A3LKD3Ma1lWQajHg\\nuGF8n3A29zIzaE3jesaJC+ZT1I11YXyqnl1jBmJ/hfwkUvF3ltq3Y0+Lz6Bjyktk\\n1u4gyNKygB779XTun3d+KNiBL52QjhIu4j6hxAyJrvhtoAX6DwWwdRXRIgLSjr3B\\n0TCugTv8WV88Zw/tFFfmCcy+m1Cwvmg2/VSBfo4QNiHGRTb+Ny/iOZc6nFd1b23r\\npJvI/Pj8/VtujjmTS+aC8iVaoTb13XmO006lYnCXWQKBgQDsmvYDX+pw44uR7HqD\\ng9246/UGdtxC99jEyLEZFbUofJuM6eY9snuiV8mtdGEZN156xE9enpxeFKopC83/\\ni3EJcrG3SQ6e+mdxXWH6Ie8Q8s/JR5yzxo0ERWpcdI2I9ISEeziOVSRBy7uiUmhQ\\nu2ljVNLnzJXpOLNtO6vx3KoBHQKBgQDKehPK99g/+lgJFAgwtdUiMFCwAIhHb/Bz\\nNh203y3Mb41vzUaPt3ufd289G7A+T6yecRMjl1/Yw8MXOka0XsAG1VjMtsoReq+P\\ngbnE/PS3Fe+GvjVe+d/204lYGST2O9Wbp0ZzfUJ3tDmzJfGoKDv7YG2arEEq1d8S\\nbW5AxUP18wKBgQCB5aFiwOnpCEO73WR1h5PWzlQ7NE5AxelQQA5agzxt+ds8Rb2N\\n6ctOmXuzqs0UFv3b0KLyg4m9bQLuffieb2OjmEBRK97HERpxdp8vwCmL2IKTMqmp\\nWTxaN/HoyGRb6EJIqY2nNr8tEDj3KN70U6/MROl1MTCsVeDL+5KuukLrUQKBgBag\\npe2c3RHpTEVXc8Ldzz/O3nWM2a4CGIEHnzJDbr7WCJxh+Gy/OJWNp2TQ31VXWhcq\\n09S6af+zgO3rmVM284bP+OiH1PU3HL+Q483b5w0+6lHIl/ZRPnI7GbI6vSK3dRu8\\nCeeGLBNslLYsm2ELfgiNsjIC5hV/aTqJ+DRM11d5AoGAMWDVwvLVG1tcT8PIJfBn\\nh5L4w4AmI9T4P8h7QvAijGUijS1Qqv06ya4LokyifMh46kSC/QLWFWlTREoVgRcy\\ngT0pOmY16CR6L8UPN1x+q6NsGTLMc5kpUwnnqzG3wr9eOW6TvSHw/vkDSDKN6370\\nZuDhmccAJYP4nV/VDb7g0cw=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-fbsvc@lunoo-78c1d.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"107817722630739223000\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40lunoo-78c1d.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";

            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream).createScoped(Lists.newArrayList(firebaseMessagingScope));

            googleCredentials.refresh();

            return googleCredentials.getAccessToken().getTokenValue();

        }catch (IOException e){
            Log.e("TAG", "getAccessToken: "+ e.getMessage());
            return null;
        }
    }
}
