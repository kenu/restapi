package com.park.restapi.util.jwt;

import com.park.restapi.domain.member.entity.Member;
import com.park.restapi.domain.member.entity.MemberRole;
import com.park.restapi.domain.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Override // 이 주소로 오는 건 토큰 없어도 됨.
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.startsWith("/api/authentications/send") || path.startsWith("/api/email-check") || path.startsWith("/api/phonenumber-check") || path.startsWith("/login")
                || path.startsWith("/api/authentications/verify") || path.startsWith("/api/signup") || path.startsWith("/api/login") || path.startsWith("/oauth2/authorization/kakao")
                || path.startsWith("/ws");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("jwt 필터 동작");

        Optional<String> accessTokenOptional = findCookieToken(request, "accessToken");
        // 쿠키 자체가 없으면 401 에러 발생
        if (!accessTokenOptional.isPresent()) {
            log.error("쿠키 없음.");
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "쿠키가 존재하지 않습니다.");
            return;
        }

        // 액세스 토큰 추출
        String accessToken = accessTokenOptional.get();

        // userId 토큰에서 꺼냄.
        try {
            TokenInfo tokenInfo = jwtService.getUserId(accessToken);
            log.info("userId:{}번 유저 토큰 추출 완료", tokenInfo.getUserId());

            // 토큰이 만료됐으면
            if (tokenInfo.isExpired()) {
                // 리프레시 토큰 추출
                Optional<String> refreshTokenOptional = findCookieToken(request, "refreshToken");
                if (!refreshTokenOptional.isPresent()) {
                    log.error("리프레시 토큰 없음");
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "리프레시 토큰이 존재하지 않습니다.");
                    return;
                }

                // 리프레시 토큰 만료 확인
                if (!jwtService.checkRefreshToken(refreshTokenOptional.get())) {
                    log.error("리프레시 토큰 문제 발생");
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "리프레시 토큰 사용 불가(로그아웃 진행)");
                    return; // 여기서 처리 종료
                }

                // 토큰 재생성 및 쿠키 설정
                if (!reGenerateToken(response, tokenInfo.getUserId())) return;

                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "JWT 재발급 완료");
                return; // 여기서 처리 종료
            }

            if (!authenticateUser(request, response, tokenInfo.getUserId())) return;

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 공통 응답 메시지
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws
            IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }

    // 쿠키에서 토큰 찾기
    private Optional<String> findCookieToken(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        return cookies == null ? Optional.empty() : Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue);
    }

    // 토큰 재생성
    private boolean reGenerateToken(HttpServletResponse response, Long userId) throws IOException {
        // 액세스 토큰을 위한 쿠키 생성
        String accessToken = jwtService.createAccessToken(userId);
        String refreshToken = jwtService.createRefreshToken(userId, true);

        if ("유저 없음".equals(refreshToken)) {
            log.error("유저 없음");
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유저 정보 조회 실패");
            return false;
        }

        setCookie(response, "accessToken", accessToken);
        setCookie(response, "refreshToken", refreshToken);

        return true;
    }

    // 쿠키에 토큰 저장
    private void setCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        log.info("쿠키 생성 및 설정 완료");
    }

    // 유저 인증
    private boolean authenticateUser(HttpServletRequest request, HttpServletResponse response, Long userId) throws IOException {
        Optional<Member> byIdLogin = memberRepository.findByIdLogin(userId);
        if (byIdLogin.isEmpty()) {
            log.info("유저 데이터 없음");
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유저 데이터 없음");
            return false;
        }

        Member member = byIdLogin.get();
        List<MemberRole> memberRoles = member.getMemberRoles();
        List<SimpleGrantedAuthority> authorities = memberRoles.stream()
                .map(memberRole -> new SimpleGrantedAuthority(memberRole.getRole().name()))
                .collect(Collectors.toList());

        // 유저 인증 객체
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(member.getId(), null, authorities);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        return true;
    }

}

