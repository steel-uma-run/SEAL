export interface TeamMember {
  id: string;
<<<<<<< Updated upstream
  fullName: string;
  email: string;
  studentCode: string;
  studentType: 'FPT' | 'EXTERNAL';
=======
  name: string;
  email: string;
  role: 'Leader' | 'Member' | 'Mentor';
  avatarUrl?: string;
>>>>>>> Stashed changes
}

export interface Team {
  id: string;
  name: string;
<<<<<<< Updated upstream
  leaderId: string;
  memberIds: string[];
  members?: TeamMember[];
  mentorId?: string;
  season?: string;
  track?: string;
=======
  season: string;
  track: string;
  members: TeamMember[];
>>>>>>> Stashed changes
}
