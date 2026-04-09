package com.example.Capstone.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Capstone.common.jwt.JwtProvider;
import com.example.Capstone.domain.RefreshToken;
import com.example.Capstone.dto.response.TokenResponse;
import com.example.Capstone.exception.BusinessException;
import com.example.Capstone.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse refresh(String refreshToken) {

        // 1. Refresh Token 유효성 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException("만료된 Refresh Token 입니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED);
        }

        // 2. DB 에서 Refresh Token 조회
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException("유효하지 않은 Refresh Token 입니다.", HttpStatus.UNAUTHORIZED));

        // 3. 만료 여부 확인
        if (savedToken.isExpired()) {
            refreshTokenRepository.delete(savedToken);
            throw new BusinessException("만료된 Refresh Token 입니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED);
        }

        // 4. 새 Access Token 발급
        Long userId = jwtProvider.getUserId(refreshToken);
        String newAccessToken = jwtProvider.generateAccessToken(userId, jwtProvider.getRole(refreshToken));

        // 5. Refresh Token 갱신
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);
        savedToken.updateToken(newRefreshToken, LocalDateTime.now().plusSeconds(604800));

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
