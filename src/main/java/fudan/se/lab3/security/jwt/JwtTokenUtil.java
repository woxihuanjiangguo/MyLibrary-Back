package fudan.se.lab3.security.jwt;

import fudan.se.lab3.domain.people.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This is a util class to use JWT.
 * We give it to you for free. :)
 *
 * @author LBW
 */
@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -3839549913040578986L;

    @Autowired
    private JwtConfigProperties jwtConfigProperties;

    private static JwtTokenUtil jwtTokenUtil;

    public JwtTokenUtil(JwtConfigProperties jwtConfigProperties) {
        this.jwtConfigProperties = jwtConfigProperties;
    }

    public String generateToken(User user,String region) {
        Map<String, Object> claims = new HashMap<>();
        if(!"".equals(region) && region != null){
            //admin part, for user, no region property
            claims.put("region",region);
        }
        return Jwts.builder().addClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfigProperties.getValidity()))
                .signWith(SignatureAlgorithm.HS512, jwtConfigProperties.getSecret()).compact();
    }

    public String getUsernameFromToken(String jwtToken) {
        return getClaimFromToken(jwtToken, Claims::getSubject);
    }

    public String getRegionFromToken(String jwtToken){
        return getAllClaimsFromToken(jwtToken).get("region",String.class);
    }

    public boolean validateToken(String jwtToken, UserDetails userDetails) {
        final String username = getUsernameFromToken(jwtToken);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken));
    }

    private Date getExpirationDateFromToken(String jwtToken) {
        return getClaimFromToken(jwtToken, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(jwtToken);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String jwtToken) {
        return Jwts.parser().setSigningKey(jwtConfigProperties.getSecret()).parseClaimsJws(jwtToken).getBody();
    }

    private boolean isTokenExpired(String jwtToken) {
        final Date expiration = getExpirationDateFromToken(jwtToken);
        return expiration.before(new Date());
    }


}
