package liquidmountain.domain.qr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This class represents a QR. It uses an external service for the generation of the QR.
 */
public class QrQrCodeMonkey extends QrGenerator {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(QrQrCodeMonkey.class);
    private static final String QR_URI = "https://qr-generator.qrcode.studio/qr/custom";
    private static final String LOGO_URI = "https://qr-generator.qrcode.studio/qr/uploadImage";
    private static final String FORM_NAME = "file";

    private LogoId logoId;

    public QrQrCodeMonkey(String data, Color fg, Color bg, Image logoImg) {
        super(data, fg, bg, logoImg);
    }

    private String hashToUrl(String data) {
        return "http://localhost:8080/" + data + "?qr=1";
    }

    private LogoId genId() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(LOGO_URI, new HttpEntity<>(buildParts(), headers),
                String.class);
        LogoIdDto logoIdDto;
        try {
            logoIdDto = new ObjectMapper().readValue(response.getBody(), LogoIdDto.class);
        } catch (IOException e) {
            LOGGER.error("When parsing a JSON string", e);
            throw new RuntimeException();
        }
        return new LogoId(logoIdDto.getFile());
    }

    private String genFilename() {
        return "whatever." + logoImg.getMediaType().getSubtype();
    }

    private MultiValueMap<String, Object> buildParts() {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        HttpHeaders imgPartHeaders = new HttpHeaders();
        imgPartHeaders.setContentType(logoImg.getMediaType());
        imgPartHeaders.setContentDispositionFormData(FORM_NAME, genFilename());
        parameters.add(FORM_NAME, new HttpEntity<>(new InputStreamResource(logoImg.getInputStream()), imgPartHeaders));
        return parameters;
    }

    @Override
    public Image gen() {
        if (logoImg != null && logoId == null) {
            logoId = genId();
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        byte[] qrImg = restTemplate.postForObject(QR_URI, new HttpEntity<>(toQrInfoDto(), headers), byte[].class);
        return new Image(new ByteArrayInputStream(qrImg), MediaType.IMAGE_PNG);
    }

    private QrInfoDto toQrInfoDto() {
        QrConfigDto qrConfigDto = new QrConfigDto();
        qrConfigDto.setBodyColor(fg.getHexColor());
        qrConfigDto.setBgColor(bg.getHexColor());
        qrConfigDto.setEye1Color(fg.getHexColor());
        qrConfigDto.setEye2Color(fg.getHexColor());
        qrConfigDto.setEye3Color(fg.getHexColor());
        qrConfigDto.setEyeBall1Color(fg.getHexColor());
        qrConfigDto.setEyeBall2Color(fg.getHexColor());
        qrConfigDto.setEyeBall3Color(fg.getHexColor());
        qrConfigDto.setLogo(logoId == null ? null : logoId.getId());
        QrInfoDto qrInfoDto = new QrInfoDto();
        qrInfoDto.setData(hashToUrl(data));
        qrInfoDto.setSize(250);
        qrInfoDto.setFile("png");
        qrInfoDto.setConfig(qrConfigDto);
        return qrInfoDto;
    }

    private static class QrInfoDto {
        private String data;
        private int size;
        private String file;
        private QrConfigDto config;

        public QrConfigDto getConfig() {
            return config;
        }

        public void setConfig(QrConfigDto config) {
            this.config = config;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    private static class QrConfigDto {
        private String bodyColor;
        private String bgColor;
        private String eye1Color;
        private String eye2Color;
        private String eye3Color;
        private String eyeBall1Color;
        private String eyeBall2Color;
        private String eyeBall3Color;
        private String logo;

        public String getBodyColor() {
            return bodyColor;
        }

        public void setBodyColor(String bodyColor) {
            this.bodyColor = bodyColor;
        }

        public String getBgColor() {
            return bgColor;
        }

        public void setBgColor(String bgColor) {
            this.bgColor = bgColor;
        }

        public String getEye1Color() {
            return eye1Color;
        }

        public void setEye1Color(String eye1Color) {
            this.eye1Color = eye1Color;
        }

        public String getEye2Color() {
            return eye2Color;
        }

        public void setEye2Color(String eye2Color) {
            this.eye2Color = eye2Color;
        }

        public String getEye3Color() {
            return eye3Color;
        }

        public void setEye3Color(String eye3Color) {
            this.eye3Color = eye3Color;
        }

        public String getEyeBall1Color() {
            return eyeBall1Color;
        }

        public void setEyeBall1Color(String eyeBall1Color) {
            this.eyeBall1Color = eyeBall1Color;
        }

        public String getEyeBall2Color() {
            return eyeBall2Color;
        }

        public void setEyeBall2Color(String eyeBall2Color) {
            this.eyeBall2Color = eyeBall2Color;
        }

        public String getEyeBall3Color() {
            return eyeBall3Color;
        }

        public void setEyeBall3Color(String eyeBall3Color) {
            this.eyeBall3Color = eyeBall3Color;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }
    }

    private static class LogoIdDto {
        private String file;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
    }

    private static class LogoId {
        private String id;

        public LogoId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
