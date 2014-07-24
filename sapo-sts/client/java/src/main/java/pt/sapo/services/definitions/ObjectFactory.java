
package pt.sapo.services.definitions;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the pt.sapo.services.definitions package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RevokableToken_QNAME = new QName("http://services.sapo.pt/definitions", "RevokableToken");
    private final static QName _ESBRoles_QNAME = new QName("http://services.sapo.pt/definitions", "ESBRoles");
    private final static QName _ESBCredentials_QNAME = new QName("http://services.sapo.pt/definitions", "ESBCredentials");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: pt.sapo.services.definitions
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RevokableToken }
     * 
     */
    public RevokableToken createRevokableToken() {
        return new RevokableToken();
    }

    /**
     * Create an instance of {@link GetRevokableTokenResponse }
     * 
     */
    public GetRevokableTokenResponse createGetRevokableTokenResponse() {
        return new GetRevokableTokenResponse();
    }

    /**
     * Create an instance of {@link ValidateTokenResponse }
     * 
     */
    public ValidateTokenResponse createValidateTokenResponse() {
        return new ValidateTokenResponse();
    }

    /**
     * Create an instance of {@link TokenInfo }
     * 
     */
    public TokenInfo createTokenInfo() {
        return new TokenInfo();
    }

    /**
     * Create an instance of {@link RevokeToken }
     * 
     */
    public RevokeToken createRevokeToken() {
        return new RevokeToken();
    }

    /**
     * Create an instance of {@link ESBCredentials }
     * 
     */
    public ESBCredentials createESBCredentials() {
        return new ESBCredentials();
    }

    /**
     * Create an instance of {@link ListRevokableTokensResponse }
     * 
     */
    public ListRevokableTokensResponse createListRevokableTokensResponse() {
        return new ListRevokableTokensResponse();
    }

    /**
     * Create an instance of {@link RevokableTokens }
     * 
     */
    public RevokableTokens createRevokableTokens() {
        return new RevokableTokens();
    }

    /**
     * Create an instance of {@link GetRevokableToken }
     * 
     */
    public GetRevokableToken createGetRevokableToken() {
        return new GetRevokableToken();
    }

    /**
     * Create an instance of {@link Services }
     * 
     */
    public Services createServices() {
        return new Services();
    }

    /**
     * Create an instance of {@link GetToken }
     * 
     */
    public GetToken createGetToken() {
        return new GetToken();
    }

    /**
     * Create an instance of {@link ESBRoles }
     * 
     */
    public ESBRoles createESBRoles() {
        return new ESBRoles();
    }

    /**
     * Create an instance of {@link RevokeTokenResponse }
     * 
     */
    public RevokeTokenResponse createRevokeTokenResponse() {
        return new RevokeTokenResponse();
    }

    /**
     * Create an instance of {@link RevokeTokenInfo }
     * 
     */
    public RevokeTokenInfo createRevokeTokenInfo() {
        return new RevokeTokenInfo();
    }

    /**
     * Create an instance of {@link GetPrimaryIdResponse }
     * 
     */
    public GetPrimaryIdResponse createGetPrimaryIdResponse() {
        return new GetPrimaryIdResponse();
    }

    /**
     * Create an instance of {@link UserInfo }
     * 
     */
    public UserInfo createUserInfo() {
        return new UserInfo();
    }

    /**
     * Create an instance of {@link ListRevokableTokens }
     * 
     */
    public ListRevokableTokens createListRevokableTokens() {
        return new ListRevokableTokens();
    }

    /**
     * Create an instance of {@link GetPrimaryId }
     * 
     */
    public GetPrimaryId createGetPrimaryId() {
        return new GetPrimaryId();
    }

    /**
     * Create an instance of {@link GetInfoCardClaims }
     * 
     */
    public GetInfoCardClaims createGetInfoCardClaims() {
        return new GetInfoCardClaims();
    }

    /**
     * Create an instance of {@link GetInfoCardClaimsResponse }
     * 
     */
    public GetInfoCardClaimsResponse createGetInfoCardClaimsResponse() {
        return new GetInfoCardClaimsResponse();
    }

    /**
     * Create an instance of {@link Claims }
     * 
     */
    public Claims createClaims() {
        return new Claims();
    }

    /**
     * Create an instance of {@link GetRolesResponse }
     * 
     */
    public GetRolesResponse createGetRolesResponse() {
        return new GetRolesResponse();
    }

    /**
     * Create an instance of {@link GetTokenResponse }
     * 
     */
    public GetTokenResponse createGetTokenResponse() {
        return new GetTokenResponse();
    }

    /**
     * Create an instance of {@link GetPrimaryIdDetailsResponse }
     * 
     */
    public GetPrimaryIdDetailsResponse createGetPrimaryIdDetailsResponse() {
        return new GetPrimaryIdDetailsResponse();
    }

    /**
     * Create an instance of {@link GetRoles }
     * 
     */
    public GetRoles createGetRoles() {
        return new GetRoles();
    }

    /**
     * Create an instance of {@link GetPrimaryIdDetails }
     * 
     */
    public GetPrimaryIdDetails createGetPrimaryIdDetails() {
        return new GetPrimaryIdDetails();
    }

    /**
     * Create an instance of {@link ValidateToken }
     * 
     */
    public ValidateToken createValidateToken() {
        return new ValidateToken();
    }

    /**
     * Create an instance of {@link Service }
     * 
     */
    public Service createService() {
        return new Service();
    }

    /**
     * Create an instance of {@link Operations }
     * 
     */
    public Operations createOperations() {
        return new Operations();
    }

    /**
     * Create an instance of {@link Claim }
     * 
     */
    public Claim createClaim() {
        return new Claim();
    }

    /**
     * Create an instance of {@link ArrayOfAttribute }
     * 
     */
    public ArrayOfAttribute createArrayOfAttribute() {
        return new ArrayOfAttribute();
    }

    /**
     * Create an instance of {@link AccountNumbers }
     * 
     */
    public AccountNumbers createAccountNumbers() {
        return new AccountNumbers();
    }

    /**
     * Create an instance of {@link Attribute }
     * 
     */
    public Attribute createAttribute() {
        return new Attribute();
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link RevokableToken }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.sapo.pt/definitions", name = "RevokableToken")
    public JAXBElement<RevokableToken> createRevokableToken(RevokableToken value) {
        return new JAXBElement<RevokableToken>(_RevokableToken_QNAME, RevokableToken.class, null, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link ESBRoles }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.sapo.pt/definitions", name = "ESBRoles")
    public JAXBElement<ESBRoles> createESBRoles(ESBRoles value) {
        return new JAXBElement<ESBRoles>(_ESBRoles_QNAME, ESBRoles.class, null, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link ESBCredentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.sapo.pt/definitions", name = "ESBCredentials")
    public JAXBElement<ESBCredentials> createESBCredentials(ESBCredentials value) {
        return new JAXBElement<ESBCredentials>(_ESBCredentials_QNAME, ESBCredentials.class, null, value);
    }

}
