let token = localStorage.getItem("token");

function showLogin() {
  document.getElementById("loginSection").style.display = "block";
  document.getElementById("registerSection").style.display = "none";
}
function showRegister() {
  document.getElementById("loginSection").style.display = "none";
  document.getElementById("registerSection").style.display = "block";
}
function logout() {
  localStorage.clear();
  token = null;
  showLogin();
  document.querySelectorAll(".dashboard").forEach(d => d.style.display = "none");
}

document.getElementById("loginForm").addEventListener("submit", async e => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  const res = await fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });
  if (!res.ok) { alert("Login failed"); return; }
  const result = await res.json();
  token = result.token;
  localStorage.setItem("token", token);
  localStorage.setItem("role", result.role);
  localStorage.setItem("name", result.name);

  document.getElementById("loginSection").style.display = "none";
  if (result.role === "CANDIDATE") {
    document.getElementById("candidateDashboard").style.display = "block";
    loadCandidateJobs();
    loadCandidateApplications();
  } else if (result.role === "EMPLOYER") {
    document.getElementById("employerDashboard").style.display = "block";
    loadEmployerJobs();
  } else {
    document.getElementById("adminDashboard").style.display = "block";
    loadAdminData();
  }
});

document.getElementById("registerForm").addEventListener("submit", async e => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  const res = await fetch("/api/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });
  if (res.ok) { alert("Registered — please login"); showLogin(); }
  else alert("Registration failed");
});

document.getElementById("resumeForm").addEventListener("submit", async e => {
  e.preventDefault();
  const formData = new FormData(e.target);
  const res = await fetch("/api/candidate/upload-resume", {
    method: "POST",
    headers: { "Authorization": "Bearer " + token },
    body: formData
  });
  if (res.ok) alert("Resume uploaded successfully");
  else alert("Resume upload failed");
});

document.getElementById("postJobForm").addEventListener("submit", async e => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  const res = await fetch("/api/employer/post-job", {
    method: "POST",
    headers: { "Content-Type": "application/json", "Authorization": "Bearer " + token },
    body: JSON.stringify(data)
  });
  if (res.ok) { alert("Job posted"); loadEmployerJobs(); } else alert("Job post failed");
});

async function loadCandidateJobs() {
  try {
    const res = await fetch("/api/jobs", { headers: { "Authorization": "Bearer " + token }});
    if (!res.ok) throw new Error("Failed to load jobs");
    const jobs = await res.json();
    const container = document.getElementById("candidateJobs");
    container.innerHTML = "";
    jobs.forEach(job => {
      const div = document.createElement("div");
      div.innerHTML = `
        <b>${escapeHtml(job.title)}</b> - ${escapeHtml(job.description)} (${escapeHtml(job.location)})<br/>
        <i>Posted by: ${escapeHtml(job.employerName || "Unknown")}</i>
        <div style="margin-top:8px;">
          <input type="file" id="resumeForJob-${job.id}" />
          <button onclick="applyJob(${job.id})">Apply</button>
        </div>
      `;
      container.appendChild(div);
    });
  } catch (err) {
    console.error(err);
    document.getElementById("candidateJobs").innerHTML = "Failed to load jobs.";
  }
}

async function applyJob(jobId) {
  const resumeInput = document.querySelector(`#resumeForJob-${jobId}`);
  if (!resumeInput || resumeInput.files.length === 0) {
    alert("Please select a resume before applying");
    return;
  }
  const formData = new FormData();
  formData.append("resume", resumeInput.files[0]);

  const res = await fetch(`/api/candidate/apply/${jobId}`, {
    method: "POST",
    headers: { "Authorization": "Bearer " + token },
    body: formData
  });
  if (res.ok) {
    alert("Applied successfully");
    loadCandidateApplications();
  } else {
    const text = await res.text();
    alert("Apply failed: " + text);
  }
}

async function loadCandidateApplications() {
  const res = await fetch("/api/candidate/applications", { headers: { "Authorization": "Bearer " + token }});
  if (!res.ok) return;
  const apps = await res.json();
  const container = document.getElementById("candidateApplications");
  container.innerHTML = "";
  apps.forEach(a => {
    const div = document.createElement("div");
    div.innerHTML = `<b>${escapeHtml(a.jobTitle)}</b> — ${escapeHtml(a.status)}`;
    container.appendChild(div);
  });
}


async function loadEmployerJobs() {
  try {
    const res = await fetch("/api/employer/jobs", { headers: { "Authorization": "Bearer " + token }});
    if (!res.ok) throw new Error("Failed to load employer jobs");
    const jobs = await res.json();
    const container = document.getElementById("employerJobs");
    container.innerHTML = "";
    jobs.forEach(j => {
      const div = document.createElement("div");
      div.innerHTML = `
        <b>${escapeHtml(j.title)}</b> - ${escapeHtml(j.description)} (${escapeHtml(j.location)})
        <div style="margin-top:6px;">
          <button onclick="deleteJob(${j.id})">Delete</button>
          <button onclick="showJobApplications(${j.id})">View Applications</button>
        </div>
        <div id="apps-for-job-${j.id}" style="margin-top:8px;"></div>
      `;
      container.appendChild(div);
    });
  } catch (err) {
    console.error(err);
    document.getElementById("employerJobs").innerHTML = "Failed to load jobs.";
  }
}

async function deleteJob(jobId) {
  if (!confirm("Delete this job?")) return;
  const res = await fetch(`/api/jobs/${jobId}`, { method: "DELETE", headers: { "Authorization": "Bearer " + token }});
  if (res.ok) { alert("Deleted"); loadEmployerJobs(); } else alert("Delete failed");
}

async function showJobApplications(jobId) {
  const res = await fetch(`/api/jobs/${jobId}/applications`, { headers: { "Authorization": "Bearer " + token }});
  if (!res.ok) { alert("Failed to load applications"); return; }
  const apps = await res.json();
  const container = document.getElementById(`apps-for-job-${jobId}`);
  container.innerHTML = "";
  apps.forEach(a => {
    const div = document.createElement("div");
    div.innerHTML = `
      <b>${escapeHtml(a.candidateName)}</b> (${escapeHtml(a.candidateEmail)}) — ${escapeHtml(a.status)}
      <div style="margin-top:4px;">
        <button onclick="downloadResume(${a.id})">Open Resume</button>
        <button onclick="updateApplicationStatus(${a.id}, 'ACCEPTED', ${jobId})">Accept</button>
        <button onclick="updateApplicationStatus(${a.id}, 'REJECTED', ${jobId})">Reject</button>
      </div>
    `;
    container.appendChild(div);
  });
}

async function downloadResume(applicationId) {
  try {
    const res = await fetch(`/api/applications/${applicationId}/resume`, {
      headers: { "Authorization": "Bearer " + token }
    });
    if (!res.ok) { alert("Failed to download resume"); return; }
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "resume_" + applicationId;
    a.click();
    window.URL.revokeObjectURL(url);
  } catch (err) {
    console.error(err);
    alert("Error downloading resume");
  }
}

async function updateApplicationStatus(appId, status, jobId) {
  const res = await fetch(`/api/applications/${appId}/status?status=${encodeURIComponent(status)}`, {
    method: "PUT",
    headers: { "Authorization": "Bearer " + token }
  });
  if (res.ok) { alert("Updated"); showJobApplications(jobId); }
  else alert("Update failed");
}

async function loadAdminData() {
  const endpoints = [
    { url: "/api/admin/users", container: "allUsers", map: item => `<b>${escapeHtml(item.name)}</b> (${escapeHtml(item.role)})` },
    { url: "/api/admin/jobs", container: "allJobs", map: item => `<b>${escapeHtml(item.title)}</b> - ${escapeHtml(item.description)} (${escapeHtml(item.location)})` },
    { url: "/api/admin/applications", container: "allApplications", map: item => `<b>${escapeHtml(item.candidateName)}</b> applied ${escapeHtml(item.jobTitle)} - ${escapeHtml(item.status)}` },
    { url: "/api/admin/stats", container: "adminStats", map: item => `Users: ${item.users || 0}, Jobs: ${item.jobs || 0}, Applications: ${item.applications || 0}` }
  ];

  for (let ep of endpoints) {
    try {
      const res = await fetch(ep.url, { headers: { "Authorization": "Bearer " + token }});
      if (!res.ok) continue;
      const data = await res.json();
      const container = document.getElementById(ep.container);
      container.innerHTML = "";
      if (Array.isArray(data)) {
        data.forEach(d => { const div = document.createElement("div"); div.innerHTML = ep.map(d); container.appendChild(div); });
      } else container.innerHTML = ep.map(data);
    } catch (err) {
      console.error(err);
      document.getElementById(ep.container).innerHTML = "Failed to load data.";
    }
  }
}

function escapeHtml(s) {
  if (!s) return "";
  return s.replaceAll('&', '&amp;')
          .replaceAll('<', '&lt;')
          .replaceAll('>', '&gt;')
          .replaceAll('"', '&quot;');
}
