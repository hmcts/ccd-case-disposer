ARG APP_INSIGHTS_AGENT_VERSION=2.5.1

# Application image
ARG PLATFORM=""

FROM hmctspublic.azurecr.io/base/java${PLATFORM}:11-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/ccd-case-disposer.jar /opt/app/

EXPOSE 4458
CMD [ "ccd-case-disposer.jar" ]
