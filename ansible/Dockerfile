ARG ANSIBLE_IMAGE_VER
FROM $ANSIBLE_IMAGE_VER

ARG project_path
ARG commit_sha
ARG commit_ref

COPY config/* ./playbooks/roles/docker_compose_app/defaults/

RUN log-metadata; update-roles
