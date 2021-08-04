package fudan.se.lab3.security.jwt;

import fudan.se.lab3.service.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Write your code to make this filter works.
 *
 * @author LBW
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private JwtUserDetailsService jwtUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getMethod().equals("OPTIONS")){
            //跨域问题
            HttpServletResponse res = (HttpServletResponse) response;
            String originHeader=((HttpServletRequest) request).getHeader("Origin");
            res.addHeader("Access-Control-Allow-Credentials", "true");
            res.addHeader("Access-Control-Allow-Origin", originHeader);
            res.addHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,PUT");
            res.addHeader("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept,Authorization,token");
            return;
        }else {
            //处理token
            String jwtToken = request.getHeader("Authorization");
            if(StringUtils.isEmpty(jwtToken)){
                filterChain.doFilter(request, response);
                return;
            }
            //token不为空
            String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            //处理授权
            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
                if(jwtTokenUtil.validateToken(jwtToken,userDetails)){
                    //授权
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }




}
