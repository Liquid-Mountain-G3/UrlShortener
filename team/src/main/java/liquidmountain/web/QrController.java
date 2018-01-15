package liquidmountain.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import liquidmountain.domain.qr.*;
import liquidmountain.repository.QrRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@Api(value = "QRs", description = "Operations pertaining to QR management")
public class QrController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QrController.class);

    @Autowired
    QrRepository qrRepository;

    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Creates a new QR associated with the given hash")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "QR created")})
    @PostMapping(value = "/api/urls/{urlHash:[a-zA-Z0-9]+}/qrs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createNewQr(@PathVariable String urlHash,
                                              @RequestPart("bg") String bg,
                                              @RequestPart("fg") String fg,
                                              @RequestPart(value = "logoImg", required = false) MultipartFile logoImg) {
        try {
            Image logoImage = null;
            if (logoImg != null) {
                logoImage = new Image(logoImg.getInputStream(), MediaType.parseMediaType(logoImg.getContentType()));
            }
            QrGenerator qrGenerator = new QrQrCodeMonkey(urlHash, new Color(fg), new Color(bg), logoImage);
            int id = qrRepository.saveQr(qrGenerator);
            HttpHeaders headers = new HttpHeaders();
            // TODO: Find a better way to do this. I don't like that the URL is hardcoded.
            headers.setLocation(new URI("http://localhost:8080/api/urls/" + urlHash + "/qrs/" + id));
            return new ResponseEntity<>("", headers, HttpStatus.CREATED);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("When creating a new QR", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieves a given QR of a given hash")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "QR retrieved correctly")})
    @GetMapping(value = "/api/urls/{urlHash:[a-zA-Z0-9]+}/qrs/{qrId:[0-9]+}")
    public ResponseEntity<byte[]> getQr(@PathVariable String urlHash, @PathVariable int qrId) {
        try {
            QrGenerator qrGenerator = qrRepository.retrieveQr(urlHash, qrId);
            Image qrImg = new QrCommand(qrGenerator).run();
            byte[] qrImgRawBytes = IOUtils.toByteArray(qrImg.getInputStream());
            qrImg.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(qrImg.getMediaType());
            return new ResponseEntity<>(qrImgRawBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("When getting a QR", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Updates a given QR of a given hash")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "QR updated")})
    @PatchMapping(value = "/api/urls/{urlHash:[a-zA-Z0-9]+}/qrs/{qrId:[0-9]+}", consumes = MediaType
            .MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> patchExistingQr(@PathVariable String urlHash, @PathVariable int qrId,
                                                  @RequestPart(value = "bg", required = false) String bg,
                                                  @RequestPart(value = "fg", required = false) String fg,
                                                  @RequestPart(value = "logoImg", required = false) MultipartFile
                                                          logoImg) {
        try {
            Color bgColor = bg == null ? null : new Color(bg);
            Color fgColor = fg == null ? null : new Color(fg);
            Image image = logoImg == null ? null : new Image(logoImg.getInputStream(), MediaType.parseMediaType
                    (logoImg.getContentType()));
            QrGenerator qrGenerator = new QrQrCodeMonkey(urlHash, fgColor, bgColor, image);
            qrRepository.update(urlHash, qrId, qrGenerator);
            if (image != null) {
                image.getInputStream().close();
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IOException e) {
            LOGGER.error("When patching a QR", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
