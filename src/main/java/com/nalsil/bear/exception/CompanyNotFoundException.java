package com.nalsil.bear.exception;

/**
 * CompanyNotFoundException
 * 기업 정보를 찾을 수 없을 때 발생하는 예외
 */
public class CompanyNotFoundException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public CompanyNotFoundException() {
        super("회사 정보를 찾을 수 없습니다.");
    }

    /**
     * 메시지를 포함한 생성자
     *
     * @param message 예외 메시지
     */
    public CompanyNotFoundException(String message) {
        super(message);
    }

    /**
     * 기업 코드를 포함한 생성자
     *
     * @param companyCode 기업 코드
     * @param cause 원인
     */
    public CompanyNotFoundException(String companyCode, Throwable cause) {
        super(String.format("회사 코드 '%s'에 해당하는 정보를 찾을 수 없습니다.", companyCode), cause);
    }

    /**
     * 기업 코드로 예외 생성
     *
     * @param companyCode 기업 코드
     * @return CompanyNotFoundException
     */
    public static CompanyNotFoundException forCode(String companyCode) {
        return new CompanyNotFoundException(String.format("회사 코드 '%s'에 해당하는 정보를 찾을 수 없습니다.", companyCode));
    }
}
