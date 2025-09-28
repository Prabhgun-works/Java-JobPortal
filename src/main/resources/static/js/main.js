let token = localStorage.getItem("token");

// ---------- Section switching ----------
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

// ---------- Login ----------
document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  const res = await fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });

  if (res.ok) {
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
      loadEmployerApplications();
    } else {
      document.getElementById("adminDashboard").style.display = "block";
      loadAdminData();
    }
  } else {
    alert("Login failed!");
  }
});

// ---------- Register ----------
document.getElementById("registerForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  const res = await fetch("/api/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });

  if (res.ok) {
    alert("Registered successfully! Please login.");
    showLogin();
  } else {
    alert("Registration failed!");
  }
});

// ---------- Candidate: Resume Upload ----------
document.getElementById("resumeForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const formData = new FormData(e.target);

  const res = await fetch("/api/candidate/upload-resume", {
    method: "POST",
    headers: { "Authorization": "Bearer " + token },
    body: formData
  });

  if (res.ok) alert("Resume uploaded!");
  else alert("Resume upload failed!");
});

// ---------- Employer: Post Job ----------
document.getElementById("postJobForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));

  const res = await fetch("/api/employer/post-job", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + token
    },
    body: JSON.stringify(data)
  });

  if (res.ok) {
    alert("Job posted successfully!");
    loadEmployerJobs();
  } else alert("Job posting failed!");
});

// ---------- Candidate Functions ----------
async function loadCandidateJobs() {
  try {
    const res = await fetch("/api/jobs", {
      headers: { "Authorization": "Bearer " + token }
    });
    if (!res.ok) throw new Error("Failed to load jobs");

    const jobs = await res.json();
    const container = document.getElementById("candidateJobs");
    container.innerHTML = "";

    // Flatten employer info to avoid nested postedJobs
    jobs.forEach(job => {
      const employerName = job.employer ? job.employer.name : "Unknown";
      const div = document.createElement("div");
      div.innerHTML = `
        <b>${job.title}</b> - ${job.description} (${job.location})
        <i>Posted by: ${employerName}</i>
        <button onclick="applyJob(${job.id})">Apply</button>
      `;
      container.appendChild(div);
    });
  } catch (err) {
    console.error(err);
    document.getElementById("candidateJobs").innerHTML = "Failed to load jobs.";
  }
}

async function applyJob(jobId) {
  const formData = new FormData();
  formData.append("resume", new Blob(["dummy content"], { type: "application/pdf" }), "resume.pdf");

  const res = await fetch(`/api/candidate/apply/${jobId}`, {
    method: "POST",
    headers: { "Authorization": "Bearer " + token },
    body: formData
  });

  if (res.ok) {
    alert("Applied successfully!");
    loadCandidateApplications();
  } else {
    alert("Application failed!");
  }
}

async function loadCandidateApplications() {
  const res = await fetch("/api/candidate/applications", {
    headers: { "Authorization": "Bearer " + token }
  });
  if (!res.ok) return;

  const apps = await res.json();
  const container = document.getElementById("candidateApplications");
  container.innerHTML = "";

  apps.forEach(a => {
    const div = document.createElement("div");
    div.innerHTML = `<b>${a.job.title}</b> - Status: ${a.status}`;
    container.appendChild(div);
  });
}

// ---------- Employer Functions ----------
async function loadEmployerJobs() {
  try {
    const res = await fetch("/api/employer/jobs", {
      headers: { "Authorization": "Bearer " + token }
    });
    if (!res.ok) throw new Error("Failed to load jobs");

    const jobs = await res.json();
    const container = document.getElementById("employerJobs");
    container.innerHTML = "";

    jobs.forEach(j => {
      const div = document.createElement("div");
      div.innerHTML = `<b>${j.title}</b> - ${j.description} (${j.location})`;
      container.appendChild(div);
    });
  } catch (err) {
    console.error(err);
    document.getElementById("employerJobs").innerHTML = "Failed to load jobs.";
  }
}

async function loadEmployerApplications() {
  const resJobs = await fetch("/api/employer/jobs", { headers: { "Authorization": "Bearer " + token } });
  if (!resJobs.ok) return;

  const jobs = await resJobs.json();
  const container = document.getElementById("employerApplications");
  container.innerHTML = "";

  for (const job of jobs) {
    const resApps = await fetch(`/api/jobs/${job.id}/applications`, { headers: { "Authorization": "Bearer " + token } });
    if (!resApps.ok) continue;
    const apps = await resApps.json();
    apps.forEach(a => {
      const div = document.createElement("div");
      div.innerHTML = `<b>${a.candidate.name}</b> applied to ${a.job.title} - Status: ${a.status}`;
      container.appendChild(div);
    });
  }
}

// ---------- Admin Functions ----------
async function loadAdminData() {
  const endpoints = [
    { url: "/api/admin/users", container: "allUsers", map: item => `<b>${item.name}</b> (${item.role})` },
    { url: "/api/admin/jobs", container: "allJobs", map: item => `<b>${item.title}</b> - ${item.description} (${item.location})` },
    { url: "/api/admin/applications", container: "allApplications", map: item => `<b>${item.candidate.name}</b> applied ${item.job.title} - ${item.status}` }
  ];

  for (let ep of endpoints) {
    try {
      const res = await fetch(ep.url, { headers: { "Authorization": "Bearer " + token } });
      if (!res.ok) throw new Error("Failed to load data");

      const data = await res.json();
      const container = document.getElementById(ep.container);
      container.innerHTML = "";

      data.forEach(d => {
        const div = document.createElement("div");
        div.innerHTML = ep.map(d);
        container.appendChild(div);
      });
    } catch (err) {
      console.error(err);
      document.getElementById(ep.container).innerHTML = "Failed to load data.";
    }
  }
}
