package com.communote.server.web.fe.portal.user.system.application;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.BaseCommandController;

import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.api.core.config.StartupProperties;
import com.communote.server.web.commons.MessageHelper;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 *
 */
public class CertificateController extends BaseCommandController {

    private String view;

    /**
     * @return the formView
     */
    public String getView() {
        return view;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if (request instanceof MultipartHttpServletRequest) {
            handleUpload((MultipartHttpServletRequest) request, response);
            request.setAttribute("uploadIsFirst", true);
        }
        return new ModelAndView(getView());
    }

    /**
     * Tries to validate and store the uploaded file.
     *
     * @param request
     *            The request.
     * @param response
     *            The response.
     */
    private void handleUpload(MultipartHttpServletRequest request, HttpServletResponse response) {
        CommonsMultipartFile file = (CommonsMultipartFile) request.getFile("certificate");
        if (file.isEmpty()) {
            MessageHelper.saveErrorMessageFromKey(request, "form.error.file.none");
            return;
        }
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(file.getInputStream());
            String alias = Integer.toString(certificate.hashCode());
            StartupProperties startupProperties = CommunoteRuntime.getInstance()
                    .getConfigurationManager().getStartupProperties();
            if (startupProperties.getTrustStore().getCertificate(alias) != null) {
                MessageHelper.saveMessageFromKey(request,
                        "client.system.application.certificate.upload.success.exists");
                return;
            }
            startupProperties.setCertificate(alias, certificate);
            MessageHelper.saveMessageFromKey(request,
                    "client.system.application.certificate.upload.success");
        } catch (CertificateParsingException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.application.certificate.upload.error.certificate.none");
        } catch (CertificateException e) {
            MessageHelper.saveErrorMessageFromKey(request, "form.error.unknown");
        } catch (IOException e) {
            MessageHelper.saveErrorMessageFromKey(request, "form.error.unknown");
        } catch (KeyStoreException e) {
            MessageHelper.saveErrorMessageFromKey(request,
                    "client.system.application.certificate.upload.error.certificate.add");
        }
    }

    /**
     * @param view
     *            the view to set
     */
    public void setView(String view) {
        this.view = view;
    }
}
