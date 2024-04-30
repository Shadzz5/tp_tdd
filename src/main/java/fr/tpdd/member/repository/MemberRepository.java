package fr.tpdd.member.repository;

import fr.tpdd.member.Member;

public interface MemberRepository {

    Member save(Member member);

    Member findById(String memberId);
}
