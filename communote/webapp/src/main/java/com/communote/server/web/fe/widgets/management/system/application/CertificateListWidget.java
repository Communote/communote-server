package com.communote.server.web.fe.widgets.management.system.application;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;

import com.communote.common.util.Pair;
import com.communote.server.api.core.application.CommunoteRuntime;
import com.communote.server.web.commons.MessageHelper;
import com.communote.server.widgets.annotations.AnnotatedMultiResultWidget;
import com.communote.server.widgets.annotations.ViewIdentifier;
import com.communote.server.widgets.annotations.WidgetAction;

/**
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@ViewIdentifier("widget.management.application.certificate.list")
public class CertificateListWidget extends
        AnnotatedMultiResultWidget<Pair<String, Pair<Certificate, Validity>>> {

    /** Remove action. */
    public static final String ACTION = "remove";

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(CertificateListWidget.class);

    /**
     * Does nothing.
     */
    @Override
    protected void initParameters() {
        // Do nothing.
    }

    @Override
    protected List<Pair<String, Pair<Certificate, Validity>>> processQueryList() {
        KeyStore keyStore = CommunoteRuntime.getInstance().getConfigurationManager()
                .getStartupProperties().getTrustStore();
        List<Pair<String, Pair<Certificate, Validity>>> results = new ArrayList<Pair<String, Pair<Certificate, Validity>>>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate certificate = keyStore.getCertificate(alias);
                if (certificate != null) {
                    Pair<Certificate, Validity> pair = new Pair<Certificate, Validity>(certificate,
                            Validity.UNKNOWN);
                    if (certificate instanceof X509Certificate) {
                        try {
                            ((X509Certificate) certificate).checkValidity();
                            pair.setRight(Validity.VALID);
                        } catch (CertificateExpiredException e) {
                            pair.setRight(Validity.NOT_VALID);
                        } catch (CertificateNotYetValidException e) {
                            pair.setRight(Validity.NOT_YET_VALID);
                        }
                    }
                    results.add(new Pair<String, Pair<Certificate, Validity>>(alias, pair));
                }
            }
        } catch (KeyStoreException e) {
            LOG.warn("Wasn't able to load aliases from key store.");
        }
        return results;
    }

    /**
     * Removes the selected certificate.
     */
    @WidgetAction(ACTION)
    public void removeCertificate() {
        String alias = getParameter("certificate");
        try {
            CommunoteRuntime.getInstance().getConfigurationManager().getStartupProperties()
                    .setCertificate(alias, null);
            MessageHelper.saveMessageFromKey(getRequest(),
                    "client.system.application.certificate.remove.success");
        } catch (KeyStoreException e) {
            MessageHelper.saveErrorMessageFromKey(getRequest(),
                    "client.system.application.certificate.remove.error");
        }
    }
}
