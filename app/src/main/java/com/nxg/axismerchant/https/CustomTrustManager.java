package com.nxg.axismerchant.https;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by sweta.shah on 5/18/16.
 */
public class CustomTrustManager implements X509TrustManager {

    /**
     * X509TrustManager instance
     */
    private final X509TrustManager originalX509TrustManager;
    /**
     * KeyStore instance
     */
    private final KeyStore trustStore;

    /**
     * @param trustStore A KeyStore containing the server certificate that should be trusted
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    public CustomTrustManager(KeyStore trustStore) throws NoSuchAlgorithmException,
            KeyStoreException {
        this.trustStore = trustStore;

        TrustManagerFactory originalTrustManagerFactory = TrustManagerFactory.getInstance("X509");
        originalTrustManagerFactory.init(this.trustStore);

        TrustManager[] originalTrustManagers = originalTrustManagerFactory.getTrustManagers();
        originalX509TrustManager = (X509TrustManager) originalTrustManagers[0];
    }

    /**
     * Given the partial or complete certificate chain provided by the peer,
     * build a certificate path to a trusted root and return if it can be validated and is trusted
     * for client SSL authentication based on the authentication type.
     *
     * @param chain    the server's certificate chain
     * @param authType the authentication type based on the client certificate
     * @throws CertificateException
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        try {
            originalX509TrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException originalException) {
            originalException.printStackTrace();
            try {
                X509Certificate[] reorderedChain = reorderCertificateChain(chain);
                CertPathValidator validator = CertPathValidator.getInstance("PKIX");
                CertificateFactory factory = CertificateFactory.getInstance("X509");
                CertPath certPath = factory.generateCertPath(Arrays.asList(reorderedChain));
                PKIXParameters params = new PKIXParameters(trustStore);
                params.setRevocationEnabled(false);
                validator.validate(certPath, params);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw originalException;
            }
        }

        /*
         * Enable to support certificate pinning.
         *  try {
         *       Certificate ca = trustStore.getCertificate("ca");
         *       PublicKey clientPubKey = ca.getPublicKey();
         *       String clientPubKeyEncoded = new BigInteger(1, clientPubKey.getEncoded())
         *       .toString(16);
         *       PublicKey serverPubKey = chain[0].getPublicKey();
         *       String serverPubKeyEncoded = new BigInteger(1, serverPubKey.getEncoded())
         *       .toString(16);
         *       if (!clientPubKeyEncoded.equalsIgnoreCase(serverPubKeyEncoded)) {
         *           throw new CertificateException("checkServerTrusted: Invalid Public key
         *           received.");
         *       }
         *   } catch (KeyStoreException e) {
         *       e.printStackTrace();
         *   }
         */

    }

    /**
     * Puts the certificate chain in the proper order.
     *
     * @param chain the certificate chain, possibly with bad ordering
     * @return the re-ordered certificate chain
     */
    private X509Certificate[] reorderCertificateChain(X509Certificate[] chain) {

        X509Certificate[] reorderedChain = new X509Certificate[chain.length];
        List<X509Certificate> certificates = Arrays.asList(chain);

        int position = chain.length - 1;
        X509Certificate rootCert = findRootCert(certificates);
        reorderedChain[position] = rootCert;

        X509Certificate cert = rootCert;
        while ((cert = findSignedCert(cert, certificates)) != null && position > 0) {
            reorderedChain[--position] = cert;
        }

        return reorderedChain;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the root certificate in a possibly out-of-order certificate chain.
     *
     * @param certificates the certificate change, possibly out-of-order
     * @return the root certificate, if any, that was found in the list of certificates
     */
    private X509Certificate findRootCert(List<X509Certificate> certificates) {
        X509Certificate rootCert = null;

        for (X509Certificate cert : certificates) {
            X509Certificate signer = findSigner(cert, certificates);
            if (signer == null || signer.equals(cert)) { // no signer present, or self-signed
                rootCert = cert;
                break;
            }
        }

        return rootCert;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the first certificate in the list of certificates that is signed by the signingCert.
     */
    private X509Certificate findSignedCert(X509Certificate signingCert,
                                           List<X509Certificate> certificates) {
        X509Certificate signed = null;

        for (X509Certificate cert : certificates) {
            Principal signingCertSubjectDN = signingCert.getSubjectDN();
            Principal certIssuerDN = cert.getIssuerDN();
            if (certIssuerDN.equals(signingCertSubjectDN) && !cert.equals(signingCert)) {
                signed = cert;
                break;
            }
        }

        return signed;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the certificate in the list of certificates that signed the signedCert.
     */
    private X509Certificate findSigner(X509Certificate signedCert,
                                       List<X509Certificate> certificates) {
        X509Certificate signer = null;

        for (X509Certificate cert : certificates) {
            Principal certSubjectDN = cert.getSubjectDN();
            Principal issuerDN = signedCert.getIssuerDN();
            if (certSubjectDN.equals(issuerDN)) {
                signer = cert;
                break;
            }
        }

        return signer;
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }
}

