package com.guo.guoaiagentbackend.quota;

import com.guo.guoaiagentbackend.exception.BusinessException;
import com.guo.guoaiagentbackend.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuotaService {

    private final QuotaRepository quotaRepository;
    private final QuotaProperties quotaProperties;

    /**
     * AI 请求前调用：独立事务提交，避免与长耗时 LLM 共事务。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void consumeAiRequestOrThrow(String username, AiQuotaKind kind) {
        if (!quotaProperties.isEnabled()) {
            return;
        }
        int limit = quotaProperties.limitFor(kind);
        var rows = quotaRepository.incrementAiUsageIfBelowLimit(username, kind.scope(), limit);
        if (rows.isEmpty()) {
            String msg = kind == AiQuotaKind.MANUS ? "超级智能体今日次数已用完" : "恋语 AI 今日次数已用完";
            throw new BusinessException(ErrorCode.OPERATION_ERROR, msg);
        }
    }

    /**
     * 与注册用户写入同一事务，便于用户名冲突时回滚 IP 计数。
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void consumeRegistrationIpOrThrow(String clientIp) {
        if (!quotaProperties.isEnabled()) {
            return;
        }
        var rows = quotaRepository.incrementRegistrationIpIfBelowLimit(
                clientIp, quotaProperties.getRegistrationsPerIpPerDay());
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "本 IP 今日注册次数已达上限");
        }
    }
}
