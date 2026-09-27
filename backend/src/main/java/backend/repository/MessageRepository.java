package backend.repository;

import backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    /**
     * Example
     * Gets the List of Mission Report from the database by the missionId and the originator.
     * @param userId The identifier of the originator.
     * @param missionId The identifier of the mission.
     * @return List<MissionReportNew>
     */
    /*
    @Query("SELECT mr FROM MissionReportNew mr WHERE mr.mission.id = :missionId AND mr.active = TRUE AND mr.originator = :userId")
    List<MissionReportNew> getMissionReportsByMissionAndUser(@Param("missionId") String missionId, @Param("userId") String userId);
    */

}
